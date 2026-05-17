package org.lushplugins.pluginupdater.common.command.response;

import revxrsal.commands.command.CommandActor;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.response.ResponseHandler;

public class StringMessageResponseHandler implements ResponseHandler<CommandActor, String> {

    @Override
    public void handleResponse(String string, ExecutionContext<CommandActor> context) {
        context.actor().reply(string);
    }
}
