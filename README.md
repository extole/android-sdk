# Android Integration
This integration guide shows you how to set up and launch an Extole program as quickly as possible with our Android SDK.

## Requirements
The Extole Android SDK supports minSdkVersion 21 or later.

## Add Extole’s Github repository and SDK library
In the `settings.gradle` of your Android project, add Extole’s Github repository:

```
maven {
    name "Extole SDK Repository"
    url "https://maven.pkg.github.com/extole/android"
    credentials {
        username = System.getenv('GITHUB_USER')
        password = System.getenv('GITHUB_PERSONAL_ACCESS_TOKEN')
    }
}
```

_Github currently requires that you input your credentials to view public assets._
In the `build.gradle` for your project add Extole’s SDK library:

```
implementation 'com.extole.mobile:android-sdk:1.0.+'
```

## Initialize Extole

Initialize Extole. We recommend that you have a single instance of the Extole class:

```
Extole.init(context = context, appName = "your-app-name")

```

A test sandbox can also be used in the example above (i.e., sandbox = “production-test”). Be sure to replace `appName = "your-app-name"` with a name that describes your application so that you can easily find it later. Optionally, it’s useful to pass a version… ` data = {"version" to "1.0"}`
Initialize global components. The following example uses the dependency injection framework [Dagger](https://dagger.dev/dev-guide/android.html):

```
@Module
@InstallIn(SingletonComponent::class)
class Configuration {
    @Singleton
    @Provides
    fun extole(@ApplicationContext context: Context): Extole =
        runBlocking(Dispatchers.IO) {
            return@runBlocking Extole.init(
                context = context, appName = "extole-mobile-test", data = mapOf("version" to "1.0"),
                sandbox = "prod-test", labels = setOf("business"),
                listenToEvents = true
            )
        }
}
```

In the `AndroidManifest.xml` file, under the tag `&lt;application>&lt;/application>` add:
```
<meta-data
  android:name="com.extole.PROGRAM_DOMAIN"
  android:value="https://share.client.com/" />
```
You need to use your program domain in order for the application to communicate with Extole.

## Exchange data with Extole
You can easily set up the SDK to send Extole information about your customers or members and the events they generate.  as well as to receive content from Extole.

### Customer Data
Send Extole information about the customer:

```
extole.identify(email, {"member_id" to "123"})
```
You can choose to pass any type of data to describe the customer. Richer data about your customers gives your marketing team the information they need to better segment your program participants and target them with appropriate campaigns.

### Event Data
Send Extole events, such as registers, signups, conversions, account openings, etc:
```
extole.sendEvent("my_event", {"key" to "values"})
```
For each event type, you can send additional data. For example, on a conversion event you may want to pass in order ID or order value and so on.

### CTA Content
CTAs such as mobile menu items can be fully customized in the My Extole Campaign Editor. Each CTA has a designated zone. The following code is an example of how to retrieve a CTA by fetching zone content:
```
val (zone, campaign) = extole.fetchZone("cta_prefetch")
runOnUiThread {
   findViewById<EditText>(R.id.cta_text)
       .setText(zone?.get("text").toString(),TextView.BufferType.NORMAL)
    findViewById<EditText>(R.id.cta_image)
       .setText(zone?.get("image").toString(),TextView.BufferType.NORMAL)
   zone.sendEvent("cta_clicked")
}

```

## In order to be able to fetch the `cta` zone, the zone should be configured in My Extole and should return JSON content containing the `image` and `text`.
Important note: We encourage you to pull CTA content from My Extole because doing so ensures that your menu item or overlay message will reflect the copy and offer you’ve configured for your campaign.

## Advanced Usage
The following topics cover advanced use cases for the Extole Android SDK. If you would like to explore any of these options, please reach out to our Support Team at support@extole.com.

### Deeplink integration
Completing a deep link integration is simple once you have integrated with a deep link provider, such as Branch. Send a mobile event to Extole, and based on the configuration of your mobile operations, our framework will execute the corresponding action.
Deep link example:

```
DeepLinkListener { linkProperties, error ->
  GlobalScope.launch {
        ServiceLocator.getExtole(this@DeeplinkActivity)
                extole.sendEvent("deeplink", linkProperties)
  }
}
```
Extole will be able to react to this based on the Campaign configuration in[ ](http://my.extole.com/)My Extole.

### Configuring Actions from Events
You can set up a specific action to occur when an event is fired. For example, when a customer taps on your menu item CTA, you may want the event to trigger an action that loads your microsite and shows the share experience.
To set up this type of configuration, you will need to work with Extole Support to set up a zone in My Extole that returns JSON configurations with conditions and actions. The SDK executes actions for conditions that are passing for a specific event:

```
{
  "operations": [
    {
      "conditions": [
        {
          "type": "EVENT",
          "event_names": [
            "cta_tap"
          ]
        }
      ],
      "actions": [
        {
          "type": "VIEW_FULLSCREEN",
          "zone_name": "microsite"
        }
      ]
    }
  ]
}
```

### Supported Actions
The following types of actions are supported by default in our SDK.
<table>
  <tr>
   <td><strong>Action Name</strong>
   </td>
   <td><strong>Description</strong>
   </td>
  </tr>
  <tr>
   <td><code>PROMPT</code>
   </td>
   <td>Display a pop-up notification native to iOS. For example, this could appear when a discount or coupon code has been successfully applied. 
   </td>
  </tr>
  <tr>
   <td><code>NATIVE_SHARING</code>
   </td>
   <td>Open the native share sheet with a predefined message and link that customers can send via SMS or any enabled social apps. 
   </td>
  </tr>
  <tr>
   <td><code>VIEW_FULLSCREEN</code>
   </td>
   <td>Trigger a full screen mobile web view. For example, this could be your microsite as configured in My Extole to display the share experience.
   </td>
  </tr>
</table>

### Custom Actions
If you would like to create custom actions beyond our defaults, use the format exhibited in the example below. Please reach out to our Support Team at [support@extole.com](mailto:support@extole.com) if you have any questions.

#### Example custom action
In this example, describe how to create example pop up, asking question sending event to extole, returns JSON, pulling custom action value

```
class CustomAction(@Se±rializedName("custom_action_value") val customParameter: String) : Action {

    companion object {
        val ACTION_TITLE = "CUSTOM_ACTION"
    }

    override suspend fun execute(event: AppEvent, extole: ExtoleInternal) {
        extole.getData()["custom_action_key"] = "custom_action_value"
    }

    override fun getType(): Action.ActionType = Action.ActionType.CUSTOM

    override fun getTitle(): String = ACTION_TITLE
}

```

#### Registering a custom action
```
Extole.registerAction("CUSTOM_ACTION", CustomAction::class.java)
```

## Appendix
### Advanced Actions
#### Load Operations

```
{
  "type": "LOAD_OPERATIONS",
  "zones": [
    "<zone_name>"
  ],
  "data": {
    "key": "value"
  }
}
```

####  Fetch
```
{
  "type": "FETCH",
  "zones": [
    "<zone_name_1>",
    "<zone_name_2>"
  ]
}
```

#### Set Log Level
```
{
  "type": "SET_LOG_LEVEL",
  "log_level": "WARN"
}
```
