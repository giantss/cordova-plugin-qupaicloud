var exec = require('cordova/exec'),
    cordova = require('cordova');

module.exports = {

    initAuth: function (successCallback, errorCallback,args) {
        if (args == null || args == undefined) {
            args = 0;
        }
        exec(successCallback, errorCallback, "QuPaiCloud", "initAuth", [args]);
    },

    recordVideo: function (successCallback, errorCallback) {
        exec(successCallback, errorCallback, "QuPaiCloud", "recordVideo", []);
    },
    upLoadVideo: function (successCallback, errorCallback, args) {
        if (args == null || args == undefined) {
            args = 0;
        }
        exec(successCallback, errorCallback, "QuPaiCloud", "upLoadVideo", [args]);
    }
};