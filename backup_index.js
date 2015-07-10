var express = require('express');
var app = express();

app.post('/data_last_update/', function (req, res) {
	var json_response = { 
		data: {
			timestamp: '800892382023'
		}
	};
	//res.send(JSON.stringify(json_response));
	res.json(json_response);
	//close server
	process.exit(0)
});

app.get('/data_last_update/', function (req, res) {
	var json_response = { 
		data: {
			timestamp: '800892382023'
		}
	};
	res.send(JSON.stringify(json_response));
	//close server
	process.exit(0)
});

app.get('/*', function (req, res) {
	var json_response = { 
		data: {
			id: '1',
			name: 'jon',
			surname: 'snow'
		},
		error: {
			code: "1",
			message: "error message"
		}
	};
	res.send(JSON.stringify(json_response));
	//close server
	process.exit(0)
});

var server = app.listen(3000, function () {

  var host = server.address().address;
  var port = server.address().port;

  console.log('Example app listening at http://%s:%s', host, port);

});