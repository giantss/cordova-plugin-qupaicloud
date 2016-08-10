

cordova.define("cordova-plugin-qupaicloud.QuPaiCloud",
    function(require, exports, module) {
        var exec = require("cordova/exec");
        module.exports = {
            upLoadVideo: function(successCallback, errorCallback,args){
            if(args == null || args == undefined){
            			args = 0;
            		}
                exec(successCallback,errorCallback,"QuPaiCloud", "upLoadVideo",[args]);
            },
			
             recordVideo: function(successCallback, errorCallback,args){
            if(args == null || args == undefined){
            			args = 0;
            		}
                exec(successCallback,errorCallback,"QuPaiCloud", "recordVideo",[]);
            },
        }
});