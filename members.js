'use strict';

var express = require('express');
var router = express.Router();

/* GET organization members */
router.get('/', function (req, res, next){
	req.ghorg.members(function (err, members){
		if(err){
			return next(err);
		} else {
			res.send(members);
		}
	});
});

/* GET member-specific issues */
router.get('/:member/issues', function (req, res, next){
	var retArr = [];
	req.ghorg.repos(function (err, repos){
		if(err){
			return next(err);
		} else {
			var nRepos = repos.length;
			repos.forEach(function (repo){
				var ghrepo = req.ghclient.repo(repo.full_name);
				ghrepo.issues(function (issuesErr, issues){
					if(issuesErr){
						return next(issuesErr);
					} else {
						for(var i = 0; i < issues.length; i++){
							if(issues[i].user.login.replace(/ /g,'').toLowerCase() === req.param('member').replace(/ /g,'').toLowerCase()){
								retArr.push(issues[i]);
							}
						}
						nRepos--;
						if(nRepos<=0){
							retArr.sort(function(issue1, issue2){
								var date1 = new Date(issue1.updated_at);
								var date2 = new Date(issue2.updated_at);
								return date1 > date2 ? -1 : date1 < date2 ? 1 : 0;
							});
							res.send(retArr);
						}
					}
				});
			});
		}
	});
});

/* GET member-specific activity */
router.get('/:member/activity', function (req, res, next){
	var retArr = [];
	req.ghclient.me().info(function (err, info){
		if(err){
			return next(err);
		} else {
			req.ghclient.get('/users/' + info.login + '/events/orgs/' + req.session.ghconfig.organization, 
				function (eventsErr, status, events) {
					if(eventsErr){
						return next(err);
					}
					else{
						var nEvents = events.length;
						events.forEach(function (event){
							if(event.actor.login.replace(/ /g,'').toLowerCase() === req.param('member').replace(/ /g,'').toLowerCase()){
								retArr.push(event);
							}
							nEvents--;
							if(nEvents <= 0){
								res.send(retArr);
							}
						});
					}
				}
			);
		}
	});
});

module.exports = router;