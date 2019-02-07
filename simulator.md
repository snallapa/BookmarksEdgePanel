# Using Samsung Edge Simulator

When I first started developing this app, I had a Samsung S6 Edge so development was fluid as I could test on a physical device. Since then, I moved on to other edgeless phones and now have to develop on the given Samsung emulator which can be a real pain to get setup. These are the steps I have to take in order to get this emulator working:

1. Add the edge simulator libs in android studio the same way you did with the SLook lib. (project settings add dependency I believe)
2. Add the given signing key with the a name of debug. The name seems to be important as I had issues when changing the name to other than debug. Also found in project settings
3. Set the looks jar to the release implementation so it is not compiled in the debug implementation. This is done so that the emulator will use the correct dependencies
4. Install the apk on an android simulator (I have used marshmallow Nexus ones and those seem to work. I am not sure about others)
5. If you use a listView then you have additional steps found in the documentation. I cannot speak to those as I have not used a listview before. 