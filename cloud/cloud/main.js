
// Use Parse.Cloud.define to define as many cloud functions as you want.
// For example:
Parse.Cloud.define("fetchProfilePicture", function(request, response) {
    var Image = require("parse-image");
    var user = request.user;
    var profilePictureURL = "https://graph.facebook.com/" + user.get("fbId") + "/picture?width=200";
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

Parse.Cloud.job("fetchThemes", function(request, status) {
    function hexToRgb(hex) {
        var result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
        return result ? {
            r: parseInt(result[1], 16),
            g: parseInt(result[2], 16),
            b: parseInt(result[3], 16)
        } : null;
    }

    function avgColor(c1, c2) {
        var c1RGB = hexToRgb(c1);
        var c2RGB = hexToRgb(c2);
        var avgRGB = {};
        avgRGB.r = parseInt((c1RGB.r + c2RGB.r)/2, 10);
        avgRGB.g = parseInt((c1RGB.g + c2RGB.g)/2, 10);
        avgRGB.b = parseInt((c1RGB.b + c2RGB.b)/2, 10);

        return rgbToHex(avgRGB);
    }

    function componentToHex(c) {
        var hex = c.toString(16);
        return hex.length == 1 ? "0" + hex : hex;
    }

    function rgbToHex(rgb) {
        return "#" + componentToHex(rgb.r) + componentToHex(rgb.g) + componentToHex(rgb.b);
    }

    var user = request.user;
    var themeUrl = "http://uigradients.com/gradients.json";
    Parse.Cloud.httpRequest({
        method: "GET",
        url: themeUrl,
        followRedirects: true
    }).then(function(response) {
        var themes = new Array();
        var Theme = Parse.Object.extend("Theme");
        var theme;
        for(var i=0 ; i < response.data.length ; i++){
            var t = response.data[i];
            theme = new Theme();
            theme.set("name", t.name.split(" ").join("") + "Theme");
            theme.set("color1", t.colors[0]);
            theme.set("color2", t.colors[1]);
            var _color = avgColor(t.colors[0], t.colors[1]);
            theme.set("color", _color);
            theme.set("refColor", _color);
            themes.push(theme);
        };
        Parse.Object.saveAll(themes, function(){
            status.success("Themes were updated successfully.");
        }, function(error){
            console.log(error);
            status.error("Uh oh, something went wrong.");
        });

    }, function(error){
        status.error("Uh oh, something went wrong.");
    });
});

Parse.Cloud.define("sendPushMessage", function(request, response) {
    var senderUser = request.user;
    var toId = request.params.toId;
    var msgType = request.params.msgType;
    var msgContent = request.params.msgContent;
    var msgId = request.params.msgId;    
    var buzzable = request.params.buzzable;

    var sid = toId + senderUser.id;
    Parse.Cloud.useMasterKey();    

    var suffixMsg;
    switch(msgType){        
        case "selfiecon":
        suffixMsg = " has sent you a selfiecon!";
        break;
        case "media":
        suffixMsg = " has sent you a photo!";
        break;
        case "youtube":
        suffixMsg = " has sent you a Youtube video!";
        break;
    	case "map":
    	suffixMsg = " has shared their location with you!";
    	break;
        case "giphy":
        suffixMsg = " has sent you a GIPHY!";
        break;
        case "recording":
        suffixMsg = " has sent you a VOICE message!";
        break;
    	case "buzz":
    	suffixMsg = " just BUZZed you!"
    	break;
    }

    var pushQuery = new Parse.Query(Parse.Installation);
    pushQuery.equalTo("user", toId);
    Parse.Push.send({
        where: pushQuery,
        data: {
	    msg_id: msgId,
	    buzzable: buzzable,
            title: "MaChat",
            alert: (msgType == "text")? msgContent : senderUser.get("fName") + suffixMsg,            
            group: sid,
            sender_img_url: senderUser.get("profilePicture").url(),
            sender_fbId: senderUser.get("fbId")
        }
    }, {
        success: function() {            
            response.success("Push success.");
        },
        error: function() {
            response.error("Push failed to send with error: " + error.message);
        }
    });
});
