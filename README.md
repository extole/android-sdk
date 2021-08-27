# Extole Android SDK

This app provides source code examples of how to:
- send an event to Extole
- get a resource (text, url, data) describing a marketing campaign configured in Extole
- share via email through Extole
- share using Android native share

Screenshot:

[<img src="https://user-images.githubusercontent.com/304224/130804856-ee7b5404-4a8d-4975-ad17-85cc0bf6e253.png" width="150">](https://github.com/extole/android-sdk/blob/master/app/src/main/java/com/extole/androidsdk/MainActivity.kt)

## Setup

1. Clone this repository
2. Open it with Android Studio
3. Run it in a simulator or real device

Note: dependencies for this project are not published to a public repository at this moment.

## Using the Extole SDK

### Initializing

```
val extole = Extole.Builder("https://extole-monitor-android.extole.io/")
    .withAppName("extole-mobile-test")
    .addAppData("version", "1.0")
    .withSandbox("prod-test")
    .withDebugEnabled(true)
    .build()
```

### Sending events

```
extole.event("purchase", mapOf(Pair("partner_user_id", "123"), Pair("cart_value", "120.30")))
```

### Get marketer configured campaign attributes

```
val emailShareMessage = extole.getText("sharing.email.message")
```

### Sharing via Email through Extole

```
val eventId = extole.emailShare("shareToEmailAddress", emailShareMessage)
```

### Native Sharing

```
extole.nativeShare("title", "shareMessage")
```
