var fo = new FlashObject("http://kent.dl.sourceforge.net/sourceforge/officefloor/HelloWorldConfigurationDemo.swf", "animationName", "1020", "614", "8", "#FFFFFF");
fo.addParam("allowScriptAccess", "sameDomain");
fo.addParam("quality", "high");
fo.addParam("scale", "noscale");
fo.addParam("loop", "false");
fo.write("flashcontent");
