const functions = require('firebase-functions');
const admin = require("firebase-admin");
admin.initializeApp(functions.config().firebase);

exports.notifyUsers = functions.region('asia-northeast1').https.onCall((data, context) => {
    var name = data.name;
    var city = data.city;
    var bloodGroup = data.blood_group;
    var locality = data.locality;
    var uid = context.auth.uid;
    
    var db = admin.firestore();
    db.collection("Cities").doc(city).collection("BloodGroup").doc(bloodGroup).collection("Users").get().then(querySnapshot => {
        querySnapshot.forEach(documentSnapshot => {
            var regToken = documentSnapshot.get(registrationToken);
            var bodyValue = "";
            var titleValue = "";
            var snippetValue = "";
            var flagValue = -1;
            //Remove flagValue from if condition with proper value in future
            if (/* document is of donor */ flagValue) {
                bodyValue = name + " is in need of " + bloodGroup + " at " + locality;
                titleValue = "Help Needed!";
                snippetValue = bloodGroup + " is needed";
                flagValue = 0;
            }
            else if (/* document is of help seeker */flagValue) {
                //Set all the values accordingly
                bodyValue = "";
                flagValue = 1;
            }
           //Send FCM to each user about blood req
            var notificationMessage = {
                token : regToken,
                notification : {
                    body : bodyValue, //name + " is in need of " + bloodGroup + " at " + locality
                    title : titleValue, //"Help Needed!"
                },
                data : {
                    snippet : snippetValue,
                    helpUID : uid,
                    flag : flagValue, //Set this flag value to : 0- message for donor, 1- message for help seeker.
                }
            }
            admin.messaging().send(notificationMessage);
        });
        return null;
    }).catch(reason => {
        //Send reason as FCM to help seeker
    });
  });