'use strict';
var Hapi = require('hapi');
var Joi = require('joi');

var Datastore = require('nedb');
var db = {}; //new Datastore({ filename: 'db/db.inc.php' });

db.users = new Datastore({ filename: './db/users.json', autoload: true });
db.strings = new Datastore({ filename: './db/strings.json', autoload: true });
db.locations = new Datastore({ filename: './db/locations.json', autoload: true });

/*
db.structures = {};
db.structures.users = new Datastore({ filename: './db/users.structure.json', autoload: true });
db.structures.strings = new Datastore({ filename: './db/strings.structure.json', autoload: true });
db.structures.locations = new Datastore({ filename: './db/locations.structure.json', autoload: true });
*/
//var user = {};
//var locations = {};
//var servizi = {servizi: [{camping: true}, {toilet: true}, {handicap: false}]};

var server = new Hapi.Server();
//var server_port = process.env.OPENSHIFT_NODEJS_PORT || 8080;
//var server_ip_address = process.env.OPENSHIFT_NODEJS_IP || 'localhost';
var server_ip_address = process.env.OPENSHIFT_NODEJS_IP || 'localhost';
var server_port = process.env.OPENSHIFT_NODEJS_PORT || 8080;

//force https if not in local mode:
/*if(server_ip_address !== 'localhost')
{
    server.ext('onRequest', function (request, reply) {
        if (request.headers['x-forwarded-proto'] === 'http') {
            return reply()
                .redirect('https://' + request.headers.host + request.url.path)
                .code(301);
        }
        return reply.continue();
    });
}*/

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

server.start(function () {
    db.users.persistence.setAutocompactionInterval(1);
    db.locations.persistence.setAutocompactionInterval(1);
    //db.users.insert({username: 'admin', password: 'password', auth: 'Basic YB+PIrFTIaPNNCwdUMbOjaFT2pcNPb/iWz2/qTJsOzCsMthyXYUT/EOl56l/Y3xQKgofBcmX6/TedylnbbpW0g==',level: 'admin', id: '1'}, function (err, newDoc) { console.log('err: ', err); console.log('newDoc: ', newDoc); });
    //db.locations.insert({id: '1', comune: 'Iseo', nome: 'Montecolo', provincia: 'BS', classificazione: {codice: '3', nominale: 'eccellente'}, servizi: {camping: true, toilet: true, handicap: false}});
    //db.locations.insert({id: '2', comune: 'Iseo', nome: 'Araba Fenice', provincia: 'BS', classificazione: {codice: '2', nominale: 'buona'}, servizi: {camping: true, toilet: true, handicap: true}});

    console.log('Server running at:', server.info.uri);
    console.log(server.info);
});

// unauthenticated route: login route

server.route({
    method: ['POST'],
    path: '/login',
    config: { auth: false },
    handler: function (request, reply) {
        var username = request.payload.username;
        var password = request.payload.password;
        //db.users.findOne({username: '')

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
    method: ['PUT'],
    path: '/location/{id}',
    config: { 
        auth: 'basic'
    },
    handler: function (request, reply) {
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
        var default_location = JSON.parse('{"id":{"nome_campo":{"default":"IT","IT":"id","EN":"id","DE":"-"}},"comune":{"nome_campo":{"default":"IT","IT":"comune","EN":"town","DE":"-"}},"localita":{"nome_campo":{"default":"IT","IT":"località","EN":"location","DE":"-"}},"provincia":{"nome_campo":{"default":"IT","IT":"provincia","EN":"province","DE":"-"}},"coordinate":{"nome_campo":{"default":"IT","IT":"coordinate","EN":"coordinate","DE":"-"},"latitudine":{"nome_campo":{"default":"IT","IT":"latitudine","EN":"latitude","DE":"-"}},"longitudine":{"nome_campo":{"default":"IT","IT":"longitudine","EN":"longitude","DE":"-"}}},"qualita_acque":{"nome_campo":{"default":"IT","IT":"qualità delle acque","EN":"water quality","DE":"-"},"stelle":{"nome_campo":{"default":"IT","IT":"stelle","EN":"stars","DE":"-"},"valore":"3"},"classificazione":{"nome_campo":{"default":"IT","IT":"classificazione","EN":"classification","DE":"-"},"valore":{"default":"IT","IT":"Eccellente","EN":"Excellent","DE":"-"}}},"descrizione_area":{"nome_campo":{"default":"IT","IT":"descrizione area","EN":"area desctiption","DE":"-"},"accesso_pubblico":{"nome_campo":{"default":"IT","IT":"accesso pubblico","EN":"pubblic access","DE":"-"},"servizio_attivo":true,"descrizione":{"nome_campo":{"default":"IT","IT":"desctrizione","EN":"description","DE":"-"},"valore":{"default":"IT","DE":"-"}}},"handicap":{"nome_campo":{"default":"IT","IT":"handicap","EN":"handicap","DE":"-"},"servizio_attivo":true,"descrizione":{"nome_campo":{"default":"IT","IT":"desctrizione","EN":"description","DE":"-"},"valore":{"default":"IT","DE":"-"}}},"parcheggio":{"nome_campo":{"default":"IT","IT":"parcheggio","EN":"parking","DE":"-"},"servizio_attivo":true,"descrizione":{"nome_campo":{"default":"IT","IT":"desctrizione","EN":"description","DE":"-"},"valore":{"default":"IT","DE":"-"}}},"servizi_igienici":{"nome_campo":{"default":"IT","IT":"servizi igienici","EN":"toilette","DE":"-"},"servizio_attivo":true,"descrizione":{"nome_campo":{"default":"IT","IT":"desctrizione","EN":"description","DE":"-"},"valore":{"default":"IT","DE":"-"}}},"area_pubblica":{"nome_campo":{"default":"IT","IT":"area pubblica","EN":"public area","DE":"-"},"servizio_attivo":true,"descrizione":{"nome_campo":{"default":"IT","IT":"desctrizione","EN":"description","DE":"-"},"valore":{"default":"IT","DE":"-"}}},"area_verde":{"nome_campo":{"default":"IT","IT":"area verde","EN":"park","DE":"-"},"servizio_attivo":true,"descrizione":{"nome_campo":{"default":"IT","IT":"desctrizione","EN":"description","DE":"-"},"valore":{"default":"IT","DE":"-"}}},"area_privata":{"nome_campo":{"default":"IT","IT":"area privata","EN":"private area","DE":"-"},"servizio_attivo":true,"descrizione":{"nome_campo":{"default":"IT","IT":"desctrizione","EN":"description","DE":"-"},"valore":{"default":"IT","DE":"-"}}},"area_pic_nic":{"nome_campo":{"default":"IT","IT":"area pic-nic","EN":"pic-nic area","DE":"-"},"servizio_attivo":true,"descrizione":{"nome_campo":{"default":"IT","IT":"desctrizione","EN":"description","DE":"-"},"valore":{"default":"IT","DE":"-"}}},"area_giochi":{"nome_campo":{"default":"IT","IT":"area giochi","EN":"playground area","DE":"-"},"servizio_attivo":true,"descrizione":{"nome_campo":{"default":"IT","IT":"desctrizione","EN":"description","DE":"-"},"valore":{"default":"IT","DE":"-"}}},"bar_ristorante":{"nome_campo":{"default":"IT","IT":"bar - ristorante","EN":"bar - restaurant","DE":"-"},"servizio_attivo":true,"descrizione":{"nome_campo":{"default":"IT","IT":"desctrizione","EN":"description","DE":"-"},"valore":{"default":"IT","DE":"-"}}},"attracco_barche":{"nome_campo":{"default":"IT","IT":"attracco barche","EN":"mooring","DE":"-"},"servizio_attivo":true,"descrizione":{"nome_campo":{"default":"IT","IT":"desctrizione","EN":"description","DE":"-"},"valore":{"default":"IT","DE":"-"}}},"piste_ciclopedonali":{"nome_campo":{"default":"IT","IT":"piste ciclopedonali","EN":"pedestrian and cycling paths","DE":"-"},"servizio_attivo":true,"descrizione":{"nome_campo":{"default":"IT","IT":"desctrizione","EN":"description","DE":"-"},"valore":{"default":"IT","DE":"-"}}},"strutture_sportive":{"nome_campo":{"default":"IT","IT":"strutture sportive","EN":"sports facilities","DE":"-"},"servizio_attivo":true,"descrizione":{"nome_campo":{"default":"IT","IT":"desctrizione","EN":"description","DE":"-"},"valore":{"default":"IT","DE":"-"}}},"sport_praticabili":{"nome_campo":{"default":"IT","IT":"sport praticabili","EN":"available sports","DE":"-"},"lista":[{"default":"IT","DE":"-"}]}},"informazioni_utili":{"nome_campo":{"default":"IT","IT":"informazioni utili","EN":"useful information","DE":"-"},"guardia_medica":{"nome_campo":{"default":"IT","IT":"guardia medica","EN":"medical service","DE":"-"},"indirizzo":{"nome_campo":{"default":"IT","IT":"indirizzo","EN":"address","DE":"-"}},"telefono":{"nome_campo":{"default":"IT","IT":"telefono","EN":"telephone","DE":"-"}}},"pronto_soccorso":{"nome_campo":{"default":"IT","IT":"pronto soccorso","EN":"emergency medical service","DE":"-"},"nome":{"nome_campo":{"default":"IT","IT":"nome","EN":"name","DE":"-"}},"indirizzo":{"nome_campo":{"default":"IT","IT":"indirizzo","EN":"address","DE":"-"}},"telefono":{"nome_campo":{"default":"IT","IT":"telefono","EN":"telephone","DE":"-"}}},"farmacia":{"nome_campo":{"default":"IT","IT":"farmacia","EN":"drugstore","DE":"-"},"nome":{"nome_campo":{"default":"IT","IT":"nome","EN":"name","DE":"-"}},"indirizzo":{"nome_campo":{"default":"IT","IT":"indirizzo","EN":"address","DE":"-"}},"telefono":{"nome_campo":{"default":"IT","IT":"telefono","EN":"telephone","DE":"-"}}},"polizia_provinciale":{"nome_campo":{"default":"IT","IT":"polizia provinciale","EN":"provincial police","DE":"-"},"indirizzo":{"nome_campo":{"default":"IT","IT":"indirizzo","EN":"address","DE":"-"}},"telefono":{"nome_campo":{"default":"IT","IT":"telefono","EN":"telephone","DE":"-"}}},"carabinieri":{"nome_campo":{"default":"IT","IT":"carabinieri","EN":"police","DE":"-"},"indirizzo":{"nome_campo":{"default":"IT","IT":"indirizzo","EN":"address","DE":"-"}},"telefono":{"nome_campo":{"default":"IT","IT":"telefono","EN":"telephone","DE":"-"}}},"polizia_locale":{"nome_campo":{"default":"IT","IT":"polizia_locale","EN":"local police","DE":"-"},"indirizzo":{"nome_campo":{"default":"IT","IT":"indirizzo","EN":"address","DE":"-"}},"telefono":{"nome_campo":{"default":"IT","IT":"telefono","EN":"telephone","DE":"-"}}}}}');
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
