package novaz.command;

import novaz.core.AbstractCommand;
import novaz.handler.TextHandler;
import novaz.main.NovaBot;

/**
 * Gemaakt op 10-8-2016
 */
public class Say extends AbstractCommand{
		public Say(NovaBot b) {
			super(b);
			setOpOnly(true);
			setCmd("say");
		}

		@Override
		public String execute(String[] args, String sender, boolean isOp) {
			boolean first=true;
			String ret = "";
			if(args.length >0){
				for(String s: args){
					if(first){
						first=false;
						ret += s;
					}
					else{
						ret += " " + s;
					}

				}
			}
			return TextHandler.get("permission_denied");
		}
}
