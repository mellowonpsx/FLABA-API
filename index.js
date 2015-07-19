'use strict';
var Hapi = require('hapi');
var Joi = require('joi');
var defaultLocation = require('./db/location.default');
var valuesWaterQuality = require('./db/values.qualita_acque');
var locationSchema = require('./db/location.schema');

var Datastore = require('nedb');
var db = {}; //new Datastore({ filename: 'db/db.inc.php' });
var fs = require('fs');
var multiparty = require('multiparty');
var publicFolder = './resource/';

db.users = new Datastore({ filename: './db/users.json', autoload: true });
db.strings = new Datastore({ filename: './db/strings.json', autoload: true });
db.locations = new Datastore({ filename: './db/locations.json', autoload: true });

var server = new Hapi.Server();
var server_ip_address = process.env.OPENSHIFT_NODEJS_IP || 'localhost';
var server_port = process.env.OPENSHIFT_NODEJS_PORT || 8080;

server.connection({
    address: server_ip_address,
    port: server_port,
    routes: {cors: true}
});

var scheme = function (server, options) {
    return {
        authenticate: function (request, reply) {
            var req = request.raw.req;
            var authorization = req.headers.authorization;
            if (!authorization) {
                return reply({
                    success: false,
                    error: {
                        code: 401,
                        message: 'auth missing'
                    }
                })
            }
            db.users.findOne({auth: authorization}, function (err, user) {
                if(err) {
                    console.error(err);
                    return reply({
                        success: false,
                        error: {
                                code: '503',
                                message: 'internal server error (database)'
                        }
                    });
                }
                if(user) {
                    return reply.continue({ credentials: { level: user.level } });
                }
                else {
                    return reply({
                        success: false,
                        error: {
                                code: '401',
                                message: 'wrong auth'
                        }
                    });
                }
            });
        }
    };
};

server.auth.scheme('custom', scheme);
server.auth.strategy('basic', 'custom');

var lastModified;

server.start(function () {
    db.users.persistence.setAutocompactionInterval(1);
    db.locations.persistence.setAutocompactionInterval(1);
    console.log('Server running at:', server.info.uri);
    console.log(server.info);
    lastModified = new Date().toUTCString();
});

// unauthenticated route: login route

server.route({
    method: ['POST'],
    path: '/login',
    config: { auth: false },
    handler: function (request, reply) {
        var username = request.payload.username;
        var password = request.payload.password;

        db.users.findOne({username: username, password: password}, function (err, user) {
            if(err) {
                console.error(err);
                return reply({
                    success: false,
                    error: {
                            code: '503',
                            message: 'internal server error (database)'
                    }
                });
            }

            if(user) {
                return reply({
                   success: true,
                   data: {
                        id: user.id,
                        username: user.username,
                        level: user.level
                    }
                });
            }
            else {
                setTimeout(function() { //setTimeout aggiunge 1 sec delay alla restituzione dell'errore
                return reply({
                    success: false,
                    error: {
                            code: '401',
                            message: 'username or password is incorrect'
                    }
                });
                }, 1000);
            }
        });
    }
});

server.route({
    method: ['GET'],
    path: '/no-auth/locations',
    config: { auth: false },
    handler: function (request, reply) {
        db.locations.find({}, function (err, locations) {
            if(err) {
                console.error(err);
                return reply({
                    success: false,
                    error: {
                            code: '503',
                            message: 'internal server error (database)'
                    }
                });
            }
            if(locations) {
                return reply({
                    success: true,
                    data: {
                        locations: locations
                    }
                }).header('last-modified', lastModified);
            }
        });
    }
});

server.route({
    method: ['GET'],
    path: '/no-auth/getfile/{filename}',
    config: { auth: false },
    handler: function (request, reply) {
        var filename = encodeURIComponent(request.params.filename);
        if(filename.indexOf('..') > -1 || filename.indexOf('/') > -1 || filename.indexOf('\\') > -1 || filename.indexOf('~') > -1) {
            console.log(filename);
            return reply({
                success: false,
                error: {
                        code: '404',
                        message: 'don\'t play with file request'
                }
            });
        }
        var filename = publicFolder+filename;
        fs.stat(filename, function(err, stats){
            if(err) {
                console.error(err);
                return reply({
                    success: false,
                    error: {
                            code: '404',
                            message: 'cannot serve the requested file'
                    }
                });
            }
            else {
                var fileLastModified = stats.mtime.toUTCString();
                return reply.file(filename).header('last-modified', fileLastModified);
            }
        });
    }
});

server.route({
    method: ['GET'],
    path: '/ping',
    config: { auth: false },
    handler: function (request, reply) {
        return reply({
            success: true,
            data: {
                message: 'this is a ping api'
            }
        });
    }
});
// auth route

server.route({
    method: ['GET','POST','PUT','DELETE'],
    path: '/',
    config: { auth: 'basic' },
    handler: function (request, reply) {
        return reply({
            success: false,
            error: {
                code: '000',
                message: 'this api is empty api'
            }
        });
    }
});

server.route({
    method: ['GET'],
    path: '/locations',
    config: { auth: 'basic' },
    handler: function (request, reply) {
        db.locations.find({}, function (err, locations) {
            if(err) {
                console.error(err);
                return reply({
                    success: false,
                    error: {
                            code: '503',
                            message: 'internal server error (database)'
                    }
                });
            }

            if(locations) {
                return reply({
                    success: true,
                    data: {
                        locations: locations
                    }
                });
            }
        });
    }
});

server.route({
    method: ['GET'],
    path: '/valori-qualita-acque',
    config: { auth: 'basic' },
    handler: function (request, reply) {
        return reply({
            success: true,
            data: {
                qualita_acque: valuesWaterQuality
            }
        });
    }
});

server.route({
    method: ['GET'],
    path: '/schema-validazione-location',
    config: { auth: 'basic' },
    handler: function (request, reply) {
        return reply({
            success: true,
            data: {
                schema: locationSchema
            }
        });
    }
});

server.route({
    method: ['PUT'],
    path: '/location/{id}',
    config: { 
        auth: 'basic'
    },
    handler: function (request, reply) {
        lastModified = new Date().toUTCString();
        var id = encodeURIComponent(request.params.id);
        db.locations.update({_id: id}, { $set: request.payload.updateValue}, function (err, numReplaced) {

            if(err) {
                console.error(err);
                return reply({
                    success: false,
                    error: {
                            code: '503',
                            message: 'internal server error (database)'
                    }
                });
            }
            if(numReplaced === 0)
            {
                return reply({
                    success: false,
                    error: {
                        code: 404,
                        message: 'element not found'
                    }
                });
            }
            else
            {
                return reply({
                    success: true,
                    data: {
                        numReplaced: numReplaced
                    }
                });
            }
        });
    }
});

server.route({
    method: ['POST'],
    path: '/location',
    config: { auth: 'basic' },
    handler: function (request, reply) {
        lastModified = new Date().toUTCString();
        var default_location = defaultLocation;//JSON.parse('{"id":{"nome_campo":{"default":"IT","IT":"id","EN":"id","DE":"-"}},"comune":{"nome_campo":{"default":"IT","IT":"comune","EN":"town","DE":"-"}},"localita":{"nome_campo":{"default":"IT","IT":"località","EN":"location","DE":"-"}},"provincia":{"nome_campo":{"default":"IT","IT":"provincia","EN":"province","DE":"-"}},"coordinate":{"nome_campo":{"default":"IT","IT":"coordinate","EN":"coordinate","DE":"-"},"latitudine":{"nome_campo":{"default":"IT","IT":"latitudine","EN":"latitude","DE":"-"}},"longitudine":{"nome_campo":{"default":"IT","IT":"longitudine","EN":"longitude","DE":"-"}}},"qualita_acque":{"nome_campo":{"default":"IT","IT":"qualità delle acque","EN":"water quality","DE":"-"},"stelle":{"nome_campo":{"default":"IT","IT":"stelle","EN":"stars","DE":"-"},"valore":"3"},"classificazione":{"nome_campo":{"default":"IT","IT":"classificazione","EN":"classification","DE":"-"},"valore":{"default":"IT","IT":"Eccellente","EN":"Excellent","DE":"-"}}},"descrizione_area":{"nome_campo":{"default":"IT","IT":"descrizione area","EN":"area desctiption","DE":"-"},"accesso_pubblico":{"nome_campo":{"default":"IT","IT":"accesso pubblico","EN":"pubblic access","DE":"-"},"servizio_attivo":true,"descrizione":{"nome_campo":{"default":"IT","IT":"desctrizione","EN":"description","DE":"-"},"valore":{"default":"IT","DE":"-"}}},"handicap":{"nome_campo":{"default":"IT","IT":"handicap","EN":"handicap","DE":"-"},"servizio_attivo":true,"descrizione":{"nome_campo":{"default":"IT","IT":"desctrizione","EN":"description","DE":"-"},"valore":{"default":"IT","DE":"-"}}},"parcheggio":{"nome_campo":{"default":"IT","IT":"parcheggio","EN":"parking","DE":"-"},"servizio_attivo":true,"descrizione":{"nome_campo":{"default":"IT","IT":"desctrizione","EN":"description","DE":"-"},"valore":{"default":"IT","DE":"-"}}},"servizi_igienici":{"nome_campo":{"default":"IT","IT":"servizi igienici","EN":"toilette","DE":"-"},"servizio_attivo":true,"descrizione":{"nome_campo":{"default":"IT","IT":"desctrizione","EN":"description","DE":"-"},"valore":{"default":"IT","DE":"-"}}},"area_pubblica":{"nome_campo":{"default":"IT","IT":"area pubblica","EN":"public area","DE":"-"},"servizio_attivo":true,"descrizione":{"nome_campo":{"default":"IT","IT":"desctrizione","EN":"description","DE":"-"},"valore":{"default":"IT","DE":"-"}}},"area_verde":{"nome_campo":{"default":"IT","IT":"area verde","EN":"park","DE":"-"},"servizio_attivo":true,"descrizione":{"nome_campo":{"default":"IT","IT":"desctrizione","EN":"description","DE":"-"},"valore":{"default":"IT","DE":"-"}}},"area_privata":{"nome_campo":{"default":"IT","IT":"area privata","EN":"private area","DE":"-"},"servizio_attivo":true,"descrizione":{"nome_campo":{"default":"IT","IT":"desctrizione","EN":"description","DE":"-"},"valore":{"default":"IT","DE":"-"}}},"area_pic_nic":{"nome_campo":{"default":"IT","IT":"area pic-nic","EN":"pic-nic area","DE":"-"},"servizio_attivo":true,"descrizione":{"nome_campo":{"default":"IT","IT":"desctrizione","EN":"description","DE":"-"},"valore":{"default":"IT","DE":"-"}}},"area_giochi":{"nome_campo":{"default":"IT","IT":"area giochi","EN":"playground area","DE":"-"},"servizio_attivo":true,"descrizione":{"nome_campo":{"default":"IT","IT":"desctrizione","EN":"description","DE":"-"},"valore":{"default":"IT","DE":"-"}}},"bar_ristorante":{"nome_campo":{"default":"IT","IT":"bar - ristorante","EN":"bar - restaurant","DE":"-"},"servizio_attivo":true,"descrizione":{"nome_campo":{"default":"IT","IT":"desctrizione","EN":"description","DE":"-"},"valore":{"default":"IT","DE":"-"}}},"attracco_barche":{"nome_campo":{"default":"IT","IT":"attracco barche","EN":"mooring","DE":"-"},"servizio_attivo":true,"descrizione":{"nome_campo":{"default":"IT","IT":"desctrizione","EN":"description","DE":"-"},"valore":{"default":"IT","DE":"-"}}},"piste_ciclopedonali":{"nome_campo":{"default":"IT","IT":"piste ciclopedonali","EN":"pedestrian and cycling paths","DE":"-"},"servizio_attivo":true,"descrizione":{"nome_campo":{"default":"IT","IT":"desctrizione","EN":"description","DE":"-"},"valore":{"default":"IT","DE":"-"}}},"strutture_sportive":{"nome_campo":{"default":"IT","IT":"strutture sportive","EN":"sports facilities","DE":"-"},"servizio_attivo":true,"descrizione":{"nome_campo":{"default":"IT","IT":"desctrizione","EN":"description","DE":"-"},"valore":{"default":"IT","DE":"-"}}},"sport_praticabili":{"nome_campo":{"default":"IT","IT":"sport praticabili","EN":"available sports","DE":"-"},"lista":[{"default":"IT","DE":"-"}]}},"informazioni_utili":{"nome_campo":{"default":"IT","IT":"informazioni utili","EN":"useful information","DE":"-"},"guardia_medica":{"nome_campo":{"default":"IT","IT":"guardia medica","EN":"medical service","DE":"-"},"indirizzo":{"nome_campo":{"default":"IT","IT":"indirizzo","EN":"address","DE":"-"}},"telefono":{"nome_campo":{"default":"IT","IT":"telefono","EN":"telephone","DE":"-"}}},"pronto_soccorso":{"nome_campo":{"default":"IT","IT":"pronto soccorso","EN":"emergency medical service","DE":"-"},"nome":{"nome_campo":{"default":"IT","IT":"nome","EN":"name","DE":"-"}},"indirizzo":{"nome_campo":{"default":"IT","IT":"indirizzo","EN":"address","DE":"-"}},"telefono":{"nome_campo":{"default":"IT","IT":"telefono","EN":"telephone","DE":"-"}}},"farmacia":{"nome_campo":{"default":"IT","IT":"farmacia","EN":"drugstore","DE":"-"},"nome":{"nome_campo":{"default":"IT","IT":"nome","EN":"name","DE":"-"}},"indirizzo":{"nome_campo":{"default":"IT","IT":"indirizzo","EN":"address","DE":"-"}},"telefono":{"nome_campo":{"default":"IT","IT":"telefono","EN":"telephone","DE":"-"}}},"polizia_provinciale":{"nome_campo":{"default":"IT","IT":"polizia provinciale","EN":"provincial police","DE":"-"},"indirizzo":{"nome_campo":{"default":"IT","IT":"indirizzo","EN":"address","DE":"-"}},"telefono":{"nome_campo":{"default":"IT","IT":"telefono","EN":"telephone","DE":"-"}}},"carabinieri":{"nome_campo":{"default":"IT","IT":"carabinieri","EN":"police","DE":"-"},"indirizzo":{"nome_campo":{"default":"IT","IT":"indirizzo","EN":"address","DE":"-"}},"telefono":{"nome_campo":{"default":"IT","IT":"telefono","EN":"telephone","DE":"-"}}},"polizia_locale":{"nome_campo":{"default":"IT","IT":"polizia_locale","EN":"local police","DE":"-"},"indirizzo":{"nome_campo":{"default":"IT","IT":"indirizzo","EN":"address","DE":"-"}},"telefono":{"nome_campo":{"default":"IT","IT":"telefono","EN":"telephone","DE":"-"}}}}}');
        db.locations.insert(default_location, function (err, newDoc) {
            if(err) {
                console.error(err);
                return reply({
                    success: false,
                    error: {
                            code: '503',
                            message: 'internal server error (database)'
                    }
                });
            }
            if(newDoc) {
                return reply({
                    success: true,
                    data: {
                        location: newDoc
                    }
                });
            }
        });
    }
});

server.route({
    method: ['GET'],
    path: '/location/{id}',
    config: { 
        auth: 'basic'
    },
    handler: function (request, reply) {
        var id = encodeURIComponent(request.params.id);
        db.locations.findOne({_id: id}, function (err, location) {
            if(err) {
                console.error(err);
                return reply({
                    success: false,
                    error: {
                            code: '503',
                            message: 'internal server error (database)'
                    }
                });
            }
            if(location)
            {
                return reply({
                    success: true,
                    data: {
                        location: location
                    }
                });
            }
            else
            {
                return reply({
                    success: false,
                    error: {
                        code: 404,
                        message: 'element not found'
                    }
                });
            }
        });
    }
});

server.route({
    method: ['DELETE'],
    path: '/location/{id}',
    config: { auth: 'basic' },
    handler: function (request, reply) {
        lastModified = new Date().toUTCString();
        var id = encodeURIComponent(request.params.id);
        db.locations.remove({_id: id}, function (err, numRemoved) {
            
            if(err) {
                console.error(err);
                return reply({
                    success: false,
                    error: {
                            code: '503',
                            message: 'internal server error (database)'
                    }
                });
            }
            if(numRemoved === 0)
            {
                return reply({
                    success: false,
                    error: {
                        code: 404,
                        message: 'element not found'
                    }
                });
            }
            else
            {
                return reply({
                    success: true,
                    data: {
                        numRemoved: numRemoved
                    }
                });
            }
        });
    }
});

server.route({
    method: 'PUT',
    path: '/putfile',
    config: {
        auth: 'basic',
        payload:{
              maxBytes: 11059200,
              output: 'stream',
              parse: false
        }   
    },
    handler: function(req,reply) {
        var form = new multiparty.Form();
        form.parse(req.payload, function(err, fields, files) {
            if(err) {
                console.error(err);
                return reply({
                    success: false,
                    error: {
                            code: '503',
                            message: 'internal server error (upload)'
                    }
                });
            }  
            else {
                fs.readFile(files.file[0].path, function(err, data) {
                    var filename = Date.parse(new Date().toUTCString()) +'-'+ files.file[0].originalFilename;
                    fs.writeFile(publicFolder + filename, data, function(err) {
                        if (err) {
                            console.error(err);
                            return reply({
                                success: false,
                                error: {
                                        code: '503',
                                        message: 'internal server error (file)'
                                }
                            });
                        }
                        else {
                            return reply({
                                success: true,
                                data: {
                                    filename: filename
                                }
                            });
                        }
                    });
                });
            }
        });
    }
});

/*server.route({
    method: 'GET',
    path: '/{name}',
    handler: function (request, reply) {
        //reply('Hello, ' + encodeURIComponent(request.params.name) + '!');
		reply({
			data: {
				id: '1',
				name: 'jon',
				surname: 'snow'
			},
			error: {
				code: "1",
				message: "error message"
			}
        }).code( 200 );
        //server.stop();
    }
});*/
