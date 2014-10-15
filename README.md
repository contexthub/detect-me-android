# DetectMe (Beacons) Sample app

The DetectMe sample app that introduces you to the beacon features of the ContextHub Android SDK.

### Table of Contents

1. **[Purpose](#purpose)**
2. **[ContextHub Use Case](#contexthub-use-case)**
3. **[Background](#background)**
4. **[Getting Started](#getting-started)**
5. **[Developer Portal](#developer-portal)**
6. **[Creating a New Context](#creating-a-new-context)**
7. **[Detecting a Beacon](#detecting-a-beacon)**
8. **[ADB Logcat](#adb-logcat)**
9. **[Adding More Beacons](#adding-more-beacons)**
10. **[Sample Code](#sample-code)**
11. **[Usage](#usage)**
  - **[Creating a Beacon](#creating-a-beacon)**
  - **[Retrieving Beacons by Tag](#retrieving-beacons-by-tag)**
  - **[Retrieving a Beacon by ID](#retrieving-a-beacon-by-id)**
  - **[Updating a Beacon](#updating-a-beacon)**
  - **[Deleting a Beacon](#deleting-a-beacon)**
  - **[Handling an Event](#handling-an-event)**
12. **[Final Words](#final-words)**

## Purpose
This sample application will show you how to create, retrieve, update, and delete (CRUD) beacons as well as respond to beacon in, out, and changed events events in ContextHub.


## ContextHub Use Case
In this sample application, we use ContextHub to interact with beacons we are aware of by registering them in the app with a tag so they autotomatically appear on every device registered with that same tag. ContextHub takes care of setting up and monitoring beacons automatically after creation and synchronization.

## Background

A "beacon" is a Bluetooth LE device which broadcasts a unique value made up of a UUID, major value, and minor value anywhere between 10-1000ms at a time. When placed in specific locations known ahead of time, beacons can be used by devices to pinpoint their location in space in areas where traditional location based methods like GPS and WiFi triangulation may not be effective.

Beacons have four important information fields that need to be programmed: UUID, major, minor, and name.
- UUID (Universally unique identifier): UUID is a 32-character hexadecimal id when used with iBeacon is typically set to be the same as a certain type or from a certain organization. For example, all iBeacons placed at a particular store location of your business would have the same UUID. UUIDs have a specific format and must be generated either using uuidgen from the [OS X Terminal](https://developer.apple.com/library/mac/documentation/Darwin/Reference/ManPages/man1/uuidgen.1.html) or on the [web](http://www.uuidgenerator.net).
- Major: This number identifies a group of beacons at a particular location. For example, all beacons on the 2nd floor or specific department of a particular location (with the same UUID) would have the same major value. This number is between 0 and 65536.
- Minor: This number uniquely identifies a specific beacon within a group of beacons with the same major value. For example, a beacon located next to the lobby would have a different minor value from a nearby elevator. Every beacon should have a unique combination of UUID, major, and minor value to distinguish between then. This number is between 0 and 65536.
- Name: Beacons are given human-readable names to easily distinguish between them. Names are not required to be unique (as uniqueness is determined by the UUID, major and minor value) however they are not allowed to have spaces.

## Getting Started

1. Get started by either forking or cloning the DetectMe repo. Visit [GitHub Help](https://help.github.com/articles/fork-a-repo) if you need help.
2. Go to [ContextHub](http://app.contexthub.com) and create a new DetectMe application.
3. Find the app id associated with the application you just created. Its format looks something like this: `13e7e6b4-9f33-4e97-b11c-79ed1470fc1d`.
4. Open up your project and put the app id into the `ContextHub.init(this, "YOUR-APP-ID-HERE")` method call in the `DetectMeApp` class.
5. Build and run the project on your device (iBeacons do not work using the Android emulator).
6. You should see a blank list view (no beacons have been entered yet)

## Developer Portal

1. Go to the [developer portal](https://app.contexthub.com) and click on your DetectMe app to access its data.
2. Click on the "Beacons" tab.  Here is a list of beacons that are present in your application (none have been created yet so it is blank). Since the beacon UUID is in a specific format, it is often easiest to enter them on a computer rather than a phone. From here you can create, update, and delete beacons. Use any number of the UUID generators on the [web](http://www.uuidgenerator.net) or UUIDgen in [OS X Terminal](https://developer.apple.com/library/mac/documentation/Darwin/Reference/ManPages/man1/uuidgen.1.html) to make a UUID, enter a major value (0-65535), minor value (0-65535), and a tag of "beacon-tag". Tags are a way to group similar ContextHub objects together during CRUD events as well as activating a specific group of objects on a user's device via subscriptions.
3. Make a beacon with the following information then click save:
```
UUID: B9407F30-F5F8-466E-AFF9-25556B57FE6D
Major: 100
Minor: 1
Tag: beacon-tag
```

## Creating a New Context

1. Contexts let you change how the server will respond to events triggered by devices. The real power of ContextHub comes from collecting and reacting to these events to perform complex actions. Let's go ahead and create a new context.
2. Click on "Contexts" tab, then click the "New Context" button to start making a new context rule.
3. Enter a name for this context which will be easy for you to remember. For now, name it "Beacon In".
4. Select the `"beacon_in"` event type. Now any event of type `"beacon_in"` will trigger this rule. You can have multiple rules with the same event type, which is why the name of events should be descriptive of the rule.
5. The Context Rule text box is where you can write a rule telling ContextHub what actions to take in response to an event triggered with the specific event type. This code is Javascript, and you have many objects at your disposal to create powerful rules: event, push, beacon, geofence, vault, http, and console. For now, put `true` the code box blank and then click save.
6. Create `"beacon_out"` and `"beacon_changed"` rules as well in the portal. A rule must exist in the dev portal before a device will generate that specific event type automatically, so this is necessary to get those type of events to fire as well.

## Detecting a Beacon

1. Now that we have our rules set up, we can test out the beacon detecting functionality. When testing, it's easiest to use an app rather than a real beacon as the range is often quite high (50 meters), making it difficult to test beacon in/out events at your computer.
2. Run the app on your Android device connected to ADB. You should see the same beacon you entered into ContextHub appear in the table view with a state of "Out" in red.
3. Setup a beacon device and program it to transmit the same UUID/major/minor/name as the beacon created above. Most Android devices cannot currently act as beacons, although this should change with the release of Android L. If you have MacBook running OS X Mavericks, try an app such as [BeaconOSX](https://github.com/mttrb/BeaconOSX). If you have an iOS device with Bluetooth 4.0, use the Locate iBeacon app. Turn on the beacon.
4. A couple of things should have just happened. First, a `beacon_in` event should have then been triggered in your debug console with the JSON representation of the event. The beacon state in the list view should have then gone from "Out" in red to "In" in green as a response to that event. Lastly, if you moved the either device after the beacon was turned on, a `beacon_changed` event should have also fired, meaning we have ranging information of the beacon with the proximity state now saying approximately how far away the test device is (immediate - within 6 inches, near - with 12 inches, far - within 50 meters).

## ADB Logcat

1. The sample app will log events into the debug console to give you an idea of the JSON structure posted to ContextHub. It will also update the status of each beacon's state once entered in ContextHub in either the app or via the developer portal.
2. Within the application class are four methods defined by the `SensorPipelineListener` interface which allow you hook into the pipeline of events generated by the device sensors. You can get notified when an event will post and has been posted, as well as control if an event should be posted and add extra payload data to an event. These four methods allow a lot of flexibility in controlling what events get sent to the server.
4. Check out the ContextHub [documentation](http://docs.contexthub.com) for more information about the event service.

## Adding More Beacons

At this point, you can add more beacons assuming you have either test devices or real beacons using either the web or the app. Entering in a UUID via the app is tricky, but the code exists there to show you how to create beacons programmatically via the ContextHub SDK.

## Sample Code

In this sample, most of the important code that deals with CRUDing beacons occurs in `BeaconListFragment` and `EditBeaconFragment`. Each method goes though a single operation you'll need to use the `BeaconProxy` classes. After each operation, a synchronization call is made so that `ProximityService` is up to date with the latest data. This method becomes unnecessary if you have properly implemented push, as background notifications will take care of synchronization for you.

In addition, `BeaconListFragment` responds to any events created from the sensor pipeline through the `SensorPipelineListener.onEventPosted()` method. At that point, you'll be able to filter whether the event was a beacon event which you were interested in and respond accordingly. There are several predefined keys that let you access information stored in an event, such as event name, state, type, etc..

## Usage

##### Creating a Beacon
```java
// Creating a beacon region with name "Beacon", tag "beacon-tag"
BeaconProxy proxy = new BeaconProxy();
proxy.createBeacon("Beacon", "B9407F30-F5F8-466E-AFF9-25556B57FE6D", 100, 1, new String[]{"beacon-tag"},
        new Callback<Beacon> {
            @Override
            public void onSuccess(Beacon result) {
                // If you do not have push properly set up, you need to explicitly call synchronize on ProximityService so it will generate events for this device
                ProximityService.getInstance().synchronize();
                Toast.makeText(getActivity(), result.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
```

##### Retrieving Beacons by Tag
```java
// Getting beacons with the tag "beacon-tag"
BeaconProxy proxy = new BeaconProxy();
proxy.listBeacons(20, new String[]{"beacon-tag"}, new Callback<List<Beacon>> {
            @Override
            public void onSuccess(List<Beacon> result) {
                for(Beacon beacon : result) {
                    Log.d(TAG, beacon.toString());
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, e.getMessage());
            }
        });
```

##### Retrieving a Beacon by ID
```java
// Getting a beacon with a specific ID
BeaconProxy proxy = new BeaconProxy();
proxy.getBeacon(1000, new Callback<Beacon> {
            @Override
            public void onSuccess(Beacon result) {
                Log.d(TAG, beacon.toString());
            }

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, e.getMessage());
            }
        });
```

##### Updating a Beacon
```java
// Updating a beacon with the name "Beacon 2" and adding the tag "park"
// In order to update a beacon, you need to pass in a valid beacon object
beacon.setName("Beacon 2");
beacon.getTags().add("park");
BeaconProxy proxy = new BeaconProxy();
proxy.updateBeacon(beacon.getId(), beacon, new Callback<Beacon> {
            @Override
            public void onSuccess(Beacon result) {
                // If you do not have push properly set up, you need to explicitly call synchronize on ProximityService so it will generate events for this device
                ProximityService.getInstance().synchronize();
                Toast.makeText(getActivity(), result.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
```

##### Deleting a Beacon
```java
final String id = "B89ECE55-9A3B-4998-AD58-2927F99802B7";
BeaconProxy proxy = new BeaconProxy();
proxy.deleteBeacon(id, new Callback<Object>() {
            @Override
            public void onSuccess(Object result) {
                // If you do not have push properly set up, you need to explicitly call synchronize on ProximityService so it will stop generating events for this device
                ProximityService.getInstance().synchronize();
                Log.d(TAG, String.format("Successfully deleted beacon id %s", id));
            }

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, e.getMessage());
            }
        });
```

##### Handling an Event
```java
@Override
public void onResume() {
    super.onResume();

    // start listening to events
    ContextHub.getInstance().addSensorPipelineListener(this);

    // enable beacon ranging
    ProximityService.getInstance().setBackgroundMode(false);
}

@Override
public void onPause() {
    super.onPause();

    // stop listening to events
    ContextHub.getInstance().removeSensorPipelineListener(this);

    // disable beacon ranging
    ProximityService.getInstance().setBackgroundMode(true);
}

@Override
public void onEventReceived(final SensorPipelineEvent event) {
    if(event.getName().startsWith("beacon_")) {
        // called on background thread, so use a Runnable to perform work on UI thread
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), event.getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

@Override
public boolean shouldPostEvent(SensorPipelineEvent event) {
    // return true to allow events to post, false to prevent them from posting
    return true;
}

@Override
public void onBeforeEventPosted(SensorPipelineEvent event) {
    // add any extra details to the event before it is posted
}

@Override
public void onEventPosted(SensorPipelineEvent event) {
    // handle an event after it has been posted to ContextHub
}
```

##### Final Words

That's it! Hopefully this sample application showed you that working with beacons in ContextHub can lead to more contextually aware applications in a shorter period of development time.