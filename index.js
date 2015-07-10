'use strict';
var Hapi = require('hapi');

var Datastore = require('nedb');
var db = {}; //new Datastore({ filename: 'db/db.inc.php' });
db.users = new Datastore({ filename: './db/users.inc.php', autoload: true });
db.locations = new Datastore({ filename: './db/locations.inc.php', autoload: true });

//var user = {};
//var locations = {};
//var servizi = {servizi: [{camping: true}, {toilet: true}, {handicap: false}]};

var server = new Hapi.Server();
//var server_port = process.env.OPENSHIFT_NODEJS_PORT || 8080;
//var server_ip_address = process.env.OPENSHIFT_NODEJS_IP || 'localhost';
var server_ip_address = process.env.OPENSHIFT_NODEJS_IP || 'localhost';
var server_port = 8080;
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
});

// unauthenticated route: login route

server.route({
    method: ['POST'],
    path: '/login/',
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
    path: '/locations/{id}/',
    config: { auth: 'basic' },
    handler: function (request, reply) {
        var id = encodeURIComponent(request.params.id);
        db.locations.update({id: id}, { $set: request.payload.updateValue}, function (err, numReplaced) {

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
    }
});

server.route({
    method: ['POST'],
    path: '/locations',
    config: { auth: 'basic' },
    handler: function (request, reply) {
        //post new document
        
        /*var id = encodeURIComponent(request.params.id);
        var posizioneElemento= locations.map(function(x) {return x.id; }).indexOf(id);
        if(posizioneElemento<0)
        {
            locations.push(request.payload.updateValue);
            return reply({
                success: true,
                data: {
                    locations: locations
                }
            });
        } else
        {
            return reply({
                success: false,
                error: {
                    code: 409,
                    message: 'conflict, element already exist'
                }
            });
        }*/
    }
});

server.route({
    method: ['DELETE'],
    path: '/locations/{id}/',
    config: { auth: 'basic' },
    handler: function (request, reply) {
        var id = encodeURIComponent(request.params.id);
        db.locations.remove({id: id}, function (err, numRemoved) {
            
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
