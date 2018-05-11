##AUTOHIT 2003
Copyright Erich P Gatejen (c) 1989,1997,2003,2004
See [LICENSE](LICENSE) for details.

See [PRODUCT DOCUMENTATION](doc/index.html) for more information.

##ABOUT
Autohit is an XML script harness targeted to testing rather than 
production systems.  It has been (still is?) used in several QA organizations
that have added an extensive array of custom modules.  The idea has been to let 
seasoned Java coders write java modules that provide the core testing
functionality, while less experienced testers could write the XML scripts to
run the tests.The XML scripts are compiled and executed in a VM.  In practice,
it has been a lot faster than one would expect from a second VM layer. 

Do not expect this to be an example of high quality, well written software.
Over time, this project turned into an experimental playground and thus 
certain designs are a bit strange.  There are a few unfinished components and
a couple seemingly useless features.  Either they were a good idea at the time
or someone specifically asked me to add it.  Not all ideas age well.

I'm releasing this system under the GPL because I don't foresee any major new
additions.  There will be maintenance releases, as my current (un-paying) 
customers ask me for fixes or small additions.  However, my intention is to
move into a new experimental playground, where the sand is fresh and the 
kids know new games.  I will not be accepting requests from anyone that
isn't using it before 5 May 2004.
