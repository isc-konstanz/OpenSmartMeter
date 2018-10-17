![emonmuc header](doc/img/emonmuc-logo.png)

This project implements a communication protocol as part of emonmuc (**e**nergy **mon**itoring **m**ulty **u**tility **c**ommunication), an *unofficial fork* of the open-source project [OpenMUC](https://www.openmuc.org/), a software framework based on Java and OSGi, that simplifies the development of customized *monitoring, logging and control* systems.


----------

# OpenIEC62056-21

*This section is a placeholder and will be filled with a project description.*


## 1 Installation

The installation guide was documented for Linux based platforms, but further guides may follow.

To setup this protocol driver, **[emonmuc](https://github.com/isc-konstanz/emonmuc/)** needs to be installed. To do so, a comprehensive guide is provided on the projects GitHub page.

With emonmuc being installed, simply copy the driver jarfile from the projects *build/lib*, to the *bundles-available* directory, by default located at `/opt/emonmuc/bundles-available`. For example:

~~~
cp ~/OpenIEC62056-21/build/libs/openmuc-driver-iec62056-21* /opt/emonmuc/bundles-available/
~~~

Now, the driver can be enabled

~~~
emonmuc enable driver iec62056-21
~~~

To disable the driver, use

~~~
emonmuc disable driver iec62056-21
~~~


### 1.1 Device templates

Next, device template files are provided by this project, to ease up the configuration of some new hardware devices.  
Those can be found at *lib/device/iec62056-21* and should be copied to the corresponding directory in the emonmuc root:

~~~
cp -R ~/OpenIEC62056-21/lib/device/iec62056-21 /opt/emonmuc/lib/device/iec62056-21
~~~


----------

# Contact

This project is maintained by:

![ISC logo](doc/img/isc-logo.png)

- **[ISC Konstanz](http://isc-konstanz.de/)** (International Solar Energy Research Center)
- **Adrian Minde**: adrian.minde@isc-konstanz.de
