package net.kaleidos.grails.plugin.security.stateless

import groovy.json.JsonBuilder

import net.kaleidos.grails.plugin.security.stateless.annotation.SecuredStateless
import org.apache.commons.lang.WordUtils
import net.kaleidos.grails.plugin.security.stateless.exception.StatelessValidationException

class SecurityStatelessFilters {

    def statelessTokenProvider

    private boolean isSecuredStateless(String controllerName, String actionName, grailsApplication){
        def controller = grailsApplication.controllerClasses.find{controllerName == WordUtils.uncapitalize(it.name)}
        if (controller) {
            def clazz = controller.clazz
            if (clazz.isAnnotationPresent(SecuredStateless)) {
                return true
            }
            if (!actionName) {
                actionName = controller.defaultAction
            }
            def method = clazz.methods.find{actionName == it.name}
            if (method) {
                return method.isAnnotationPresent(SecuredStateless)
            }
        }
        return false
    }


   def filters = {
        statelessFilter(controller:'*', action:'*') {
            before = {
                if (!isSecuredStateless(controllerName, actionName, grailsApplication)) {
                    return
                }

                def authorization = request.getHeader("Authorization")
                def map
                try {
                    map = statelessTokenProvider.validateAndExtractToken(authorization)
                } catch (StatelessValidationException e) {
                    Closure getJsonErrorBytes = { String error ->
                        Map errorMap = [message: error]
                        String jsonMap = (new JsonBuilder(errorMap)).toString()
                        return jsonMap.bytes
                    }
                    response.status = 401
                    response.outputStream << getJsonErrorBytes(e.message)
                    return false
                }

                if (map) {
                    request.securityStatelessMap = map
                    return true
                }
                response.status = 401
                return false
            }
        }
   }
}
