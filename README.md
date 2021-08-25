# Extole Android SDK

## Setup

1. Clone this repository
2. Open it with Android Studio
3. Run it in a simulator or real device

Note: dependencies for this project are not published to a public repository at this moment.

## Using Extole SDK

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
extole.event("purchase", mapOf(Pair("partner_user_id", "123"), Pair("cart_value", "120.3")))
```

### Get a text configured in your Extole Creative

```
val emailShareMessage = extole.getText("sharing.email.message")
```

### Sharing via Email

```
val eventId = extole.emailShare("shareToEmailAddress", emailShareMessage)
```

## Demo application
Demo application is very simple, it shows basic operations like:
- sending an event
- email share
- getting a resource (text or url) from your configured Extole Creative
- native sharing

Screenshot:

<img src="https://user-images.githubusercontent.com/304224/130804856-ee7b5404-4a8d-4975-ad17-85cc0bf6e253.png" width="150">
