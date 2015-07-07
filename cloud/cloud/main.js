
// Use Parse.Cloud.define to define as many cloud functions as you want.
// For example:
Parse.Cloud.define("fetchProfilePicture", function(request, response) {
    var Image = require("parse-image");
    var user = request.user;
    var profilePictureURL = "https://graph.facebook.com/" + user.get("fbId") + "/picture?width=9999";
    Parse.Cloud.httpRequest({
        url: profilePictureURL,
        followRedirects: true
    }).then(function(response) {
        // Create an Image from the data.
        var image = new Image();
        return image.setData(response.buffer);
    }).then(function(image) {
        // Scale the image to a certain size.
        return image;
    }).then(function(image) {
        // Get the bytes of the new image.
        return image.data();
    }).then(function(buffer) {
        // Save the bytes to a new file.
        var file = new Parse.File("fbpp.jpg", {
            base64: buffer.toString("base64")
        });
        file.save().then(function() {
            user.set("profilePicture", file);
            return user.save().then(function() {
                response.success("fetched successfully!")
            }, function(error) {
                response.error("fetch failed!")
            });
        }, function(error) {
            response.error("fetch failed!")
        });
    });
});

Parse.Cloud.define("getFacebookImgByfbId", function(request, response) {
    var senderUser = request.user;
    var fbId = request.params.fbId;
    Parse.Cloud.useMasterKey();    

    var query = new Parse.Query(Parse.User);
    query.equalTo("fbId", fbId);    
    query.limit(1);
    query.find({
	  success: function(people) {
	  	var person = people[0];	  	
	  	var profilePicture = person.get("profilePicture");
	  	person.fbUrl = profilePicture.url();
	  	console.log(person);
	    response.success(JSON.stringify(person));
	  }, error: function(error){
        response.error("get by fbId error: " + error.message);
      }
	});
});
