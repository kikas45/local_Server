package sync2app.com.syncapplive.additionalSettings.utils


class Constants {
    companion object {

        // This is important for master domain urls from the Json
        /// NOTE: The url must end with " / "  else the app will crash, ensure to always add the " / " at the end of the url


        /// ----  const val BASE_URL_OF_MASTER_DOMAIN = "https://cp.cloudappserver.co.uk/app_base/public/CLO/DE_MO_2021001/DOM/Custom.Json/"


        // for Cloud App sync Server Time used in Schedule media, a full path include Company and location + an end with " Servertime"
        // e.g   "https://cloudappserver.co.uk/cp/app_base/public/CLO/DE_MO_2021001/Servertime
        /// ---  const val CLOUD_APP_SYNC_SERVER_TIME_BASE_URL = "https://cloudappserver.co.uk/cp/app_base/public" + "/"




        //Custom Server Domain
        const val CP_OR_AP_MASTER_DOMAIN = "CP_AP_MASTER_DOMAIN"
        const val CUSTOM_CP_SERVER_DOMAIN = "https://cp.cloudappserver.co.uk/app_base/public/"
        const val CUSTOM_API_SERVER_DOMAIN = "https://cloudapp.web-adm.in/public/"

        const val SEVER_APP_CONFIG_END_POINT = "/App/Config/appConfig.json"
        const val App_Config_End_Point = "/App/Config"


        // for Tv or App Mode Json settings,  the BASE ( master Url )  + this  END Path makes a full url
        const val END_PATH_OF_TV_MODE_URL = "/AppConfig/InstallAppSettings.json"




        // To access Storage
        const val MANAGE_EXTERNAL_STORAGE_PERMISSION = "android:manage_external_storage"
        const val NOT_APPLICABLE = "N/A"
        const val READ_EXTERNAL_STORAGE_PERMISSION_REQUEST = 2


        //for Api CSv Download
        const val myCSvEndPath = "Start/start1.csv"
        const val ifEndContainsIndexFileName = "index.html"
        const val myCSVUpdate1 = "/Api/update1.csv"


        // for modifying the Json URL
        const val get_UserID = "get_UserID"
        const val get_LicenseKey = "get_LicenseKey"
        const val get_editTextMaster = "get_editTextMaster"
        const val baseUrl = "baseUrl"
        const val get_masterDomain = "get_masterDomain"
        const val get_APP_CONFIG_JSON_SEVER_URL = "get_APP_CONFIG_JSON_SEVER_URL"


        const val SHARED_BIOMETRIC = "SHARED_BIOMETRIC"
        const val SHARED_CAMERA_PREF = "SHARED_CAMERA_PREF"
        const val SHARED_TV_APP_MODE = "SHARED_TV_APP_MODE"
        const val HARD_WARE_DATA = "HARD_WARE_DATA"
        const val SHARED_SAVED_CRASH_REPORT = "SHARED_SAVED_CRASH_REPORT"
        const val SIMPLE_SAVED_PASSWORD = "SIMPLE_SAVED_PASSWORD"
        const val getFolderClo = "getFolderClo"
        const val getFolderSubpath = "getFolderSubpath"
        const val SettingsPage = "SettingsPage"
        const val WebViewPage = "WebViewPage"
        const val AdditionNalPage = "AdditionNalPage"
        const val file_explorer_prefs = "file_explorer_prefs"



        // web view launching state
        const val get_Launching_State_Of_WebView = "get_Launching_State_Of_WebView"
        const val launch_WebView_Online = "launch_WebView_Online"
        const val launch_WebView_Offline = "launch_WebView_Offline"
        const val launch_WebView_Online_Manual_Index = "launch_WebView_Online_Manual_Index"
        const val launch_WebView_Offline_Manual_Index = "launch_WebView_Offline_Manual_Index"
        const val launch_Default_WebView_url = "launch_Default_WebView_url"

        const val JSON_MAIN_URL = "JSON_MAIN_URL"


        const val compleConfiguration = "You are yet to complete your App configuration"
        const val UnableToFindIndex = "Launch file Not Found"
        const val Check_Inter_Connectivity = "Check Inter Connectivity"


        const val SynC_Status = "SynC_Status"
        const val Config = "Config.zip"
        const val AppConfigFolder = "AppConfig"
        const val Syn2AppLive = "Syn2AppLive"
        const val Invalid_Config_Url = "Invalid Config Url"
        const val TEMP_PARS_FOLDER = "TEMP_PARS_FOLDER"

        // for sync Api
        const val numberOfFiles = "numberOfFiles"
        const val filesChange = "filesCounts"
        const val textDownladByes = "textDownladByes"
        const val totalFilesFromApi = "totalFilesFromApi"
        const val progressBarPref = "progressBarPref"


        const val Error_during_zip_extraction = "Error during extraction"
        const val getTimeDefined = "getTimeDefined"
        const val CurrentServerTime_for_IndexChange = "CurrentServerTime_for_IndexChange"
        const val SeverTimeSaved_For_IndexChange = "SeverTimeSaved_For_IndexChange"


        const val SeverTimeSaved = "SeverTimeSaved"
        const val CurrentServerTime = "CurrentServerTime"
        const val media_ready = "Media ready"
        const val GroundPath = "groupPath"
        const val Manage_My_Sync_Start = "Manage_My_Sync_Start"



        const val Saved_Domains_Name = "Saved_Domains_Name"
        const val Saved_Domains_Urls = "Saved_Domains_Urls"
        const val Saved_Parthner_Name = "Saved_Parthner_Name"
        const val imageUseBranding = "imageUseBranding"
        const val imgToggleImageBackground = "imgToggleImageBackground"
        const val imagSwtichUseIndexCahngeOrTimeStamp = "imagSwtichUseIndexCahngeOrTimeStamp"
        const val check_if_index_chnage_is_enabled = "check_if_index_chnage_is_enabled"



        const val Tapped_OnlineORoffline = "Tapped_OnlineORoffline"

        const val tapped_launchOnline = "tapped_launchOnline"

        const val tapped_launchOffline = "tapped_launchOffline"


        const val syncUrl = "syncUrl"
        const val PASS_URL = "PASS_URL"
        const val crashCalled = "crashCalled"
        const val crashInfo = "crashInfo"
        const val QR_CODE_KEY = "QR_CODE_KEY"
        const val App_Mode = "App_Mode"
        const val TV_Mode = "TV_Mode"
        const val FIRST_TIME_APP_START = "FIRST_TIME_APP_START"
        const val FIRST_INFORMATION_PAGE_COMPLETED = "FIRST_INFORMATION_PAGE_COMPLETED"
        const val ALL_FOLDER_DELETE = "ALL_FOLDER_DELETE"
        const val USE_OFFLINE_FOLDER = "USE_OFFLINE_FOLDER"
        const val getSavedCLOImPutFiled = "getSavedCLOImPutFiled"
        const val getSaveSubFolderInPutFiled = "getSaveSubFolderInPutFiled"
        const val getSavedEditTextInputSynUrlZip = "getSavedEditTextInputSynUrlZip"
        const val getSaved_manaul_index_edit_url_Input = "getSaved_manaul_index_edit_url_Input"


        // use for Indicator Panel
        const val showDownloadSyncStatus = "showDownloadSyncStatus"
        const val imagShowOnlineStatus = "imagShowOnlineStatus"
        const val img_Make_OnlineIndicator_Default_visible = "img_Make_OnlineIndicator_Default_visible"
        const val imgStartAppRestartOnTvMode = "imgStartAppRestartOnTvMode"

        const val MY_TV_OR_APP_MODE = "MY_TV_OR_APP_MODE"
        const val SAVE_NAVIGATION = "SAVE_NAVIGATION"
        const val CALL_RE_SYNC_MANGER = "CALL_RE_SYNC_MANGER"

        const val shortcut_website_id = "id_website"
        const val shortcut_messages_id = "id_messages"

        // modified end path for master Domain
        const val get_ModifiedUrl = "get_ModifiedUrl"
      //  const val ENABLE_LANDSCAPE_MODE = "ENABLE_LANDSCAPE_MODE"
        const val ENABLE_POTRAIT_MODE = "ENABLE_POTRAIT_MODE"
        const val ENABLE_USER_AGENT = "ENABLE_USER_AGENT"
        const val PROTECT_PASSWORD = "PROTECT_PASSWORD"





        const val imgAllowFingerPrint = "imgAllowFingerPrint"
        const val imgToggleImageSplashOrVideoSplash = "imgToggleImageSplashOrVideoSplash"
        const val imgEnableAutoBoot = "imgEnableAutoBoot"
        const val BattryOptimzationOkay = "BattryOptimzationOkay"


        // managing download on Re- Sync page
        const val fil_CLO = "fil_CLO"
        const val fil_DEMO = "fil_DEMO"
        const val onCreatePasswordSaved = "onCreatePasswordSaved"
        const val imgAllowLunchFromOnline = "imgAllowLunchFromOnline"
        const val img_Let_offline_load_Listner = "img_Let_offline_load_Listner"
        const val imagSwtichEnableManualOrNot = "imagSwtichEnableManualOrNot"
        const val imagSwtichPartnerUrl = "imagSwtichPartnerUrl"
        const val imagSwtichEnableConfigFileOnline = "imagSwtichEnableConfigFileOnline"
        const val imgStartFileFirstSync = "imgStartFileFirstSync"
        const val imagSwtichEnableSyncOnFilecahnge = "imagSwtichEnableSyncOnFilecahnge"
        const val imagSwtichEnablEnableToggleOrNot = "imagSwtichEnablEnableToggleOrNot"

        const val Did_User_Input_PassWord = "Did_User_Input_PassWord"

        const val imagSwtichEnableSyncFromAPI = "imagSwtichEnableSyncFromAPI"
        const val imgEnableLockScreen = "imgEnableLockScreen"
        const val MY_DOWNLOADER_CLASS = "MY_DOWNLOADER_CLASS"
        const val downloadKey = "downloadKey"
        const val Extracted = "Extracted"
        const val App = "App"
        const val Zip = "Zip"
        const val threeFolderPath = "threeFolderPath"
        const val fileNmae_App_Zip = "App.zip"
        const val Error_CSv_Message = "Api not readable from location"
        const val Error_IndexFile_Message = "Unable to scan index file"
        const val Error_Parsing_Message = "Index File not readable from location"

        const val PR_running = "PR: Running"
        const val PR_checking = "PR: Checking"
        const val PR_Refresh = "PR: Refresh"
        const val PR_Downloading = "PR: Downloading"
        const val PR_Extracting = "PR: Extracting"
        const val Extracting = "Extracting"
        const val PR_Zip_error = "PR: Zip error"
        const val PR_Change_Found = "PR: Change Found"
        const val PR_NO_CHange = "PR: No Change"
        const val RetryCount = "RetryCount"
        const val PR_Indexing_Files = "PR: Indexing.."
        const val PR_Retry_Failed = "PR: Retrying.."
        const val PR_Failed_Files_Number = "PR:Failed Files Number"
        const val RE_START_PARSING = "RE_START_PARSING"
        const val LastUpdatedTime = "LastUpdatedTime"




        /// For Sync Intervals
        const val t_1min = 1L
        const val t_2min = 2L
        const val t_5min = 5L
        const val t_10min = 10L
        const val t_15min = 15L
        const val t_30min = 30L
        const val t_60min = 60L
        const val t_120min = 120L
        const val t_180min = 180L
        const val t_240min = 240L


        const val timeForConnection = 1L*60L*1000L

        // For Gmail

        const val Sender_email_Address = "SmtpSync2app@gmail.com"
        const val Sender_email_Password = "uhsxlgyoqhwsaihs"
        const val mailhost = "smtp.gmail.com"
        const val Sender_name = "sync2App"
        const val Subject = "User Password Request Rest"
        const val COMPANY_EMAIL = "verify@cloudappserver.co.uk"


        // for Android Download Manager Api Download
        const val fileNumber = "FileNumber"
        const val folderName = "folderName"
        const val fileName = "fileName"

        //for camera
        const val startY = "startY"
        const val camHeight = "camHeight"
        const val startX = "startX"
        const val camWidth = "camWidth"


        // stream video
        const val imgStreamVideo = "imgStreamVideo"
        const val imgEnableExpandFloat = "imgEnableExpandFloat"
        const val imgStreamAPIorDevice = "imgStreamAPIorDevice"
        const val imgStreamAudioSound = "imgStreamAudioSound"
        const val imgEnableDisplayIntervals = "imgEnableDisplayIntervals"
        const val imgUseDevicecameraOrPlugInCamera = "imgUseDevicecameraOrPlugInCamera"

        const val get_Display_Camera_Defined_Time_for_Device = "get_Display_Camera_Defined_Time_for_Device"
        const val get_Hide_Camera_Defined_Time_for_Device = "get_Hide_Camera_Defined_Time_for_Device"
        const val start_height_api = "start_height_api"
        const val end_height_api = "end_height_api"
        const val start_width_api = "start_width_api"
        const val end_width_api = "end_width_api"
        const val display_time_api = "display_time_api"
        const val hide_time_api = "hide_time_api"

        // init App settings
        const val hidebottombar = "hidebottombar"
        const val fullscreen = "fullscreen"
        const val immersive_mode = "immersive_mode"
        const val permission_query = "permission_query"
        const val nativeload = "nativeload"
        const val loadLastUrl = "loadLastUrl"
        const val blockAds = "blockAds"
        const val geolocation = "geolocation"
        const val darktheme = "darktheme"
        const val nightmode = "nightmode"
        const val swiperefresh = "swiperefresh"
        const val surl = "surl"
        const val hideQRCode = "hideQRCode"
        const val hide_drawer_icon = "hide_drawer_icon"
        const val autohideToolbar = "autohideToolbar"
        const val enableCacheMode = "enableCacheMode"
        const val shwoFloatingButton = "shwoFloatingButton"

        // for short cut
        const val Do_NO_SHOW_SHORT_CUT_AGAIN = "Do_NO_SHOW_SHORT_CUT_AGAIN"

        // Sync USb CAmera
        const val SYNC_CAMERA_DISCONNECTED= "sync2app.camera.DISCONNECTED"

        // allow didplay over
        const val isDislayOverAllowed= "isDislayOverAllowed"



        // For Refresh Time
        const val get_Refresh_Timer = "get_Refresh_Timer"
        const val T_3_HR = 3L
        const val T_4_HR = 4L
        const val T_5_HR = 5L
        const val T_6_HR = 6L
        const val T_7_HR = 7L
        const val T_8_HR = 8L
        const val T_9_HR = 9L
        const val T_10_HR = 10L
        const val T_11_HR = 11L
        const val T_12_HR = 12L


        //// for password
        const val passowrdPrefeilled = "passowrdPrefeilled"
        const val KEY_FUTURE_TIME = "keyFutureTime"
        const val KEY_FUTURE_TIME_FIRST = "KEY_FUTURE_TIME_FIRST"
        const val IN_VALID_EMAIL = "Please enter a valid email"



        // For Refresh Time
        const val REFRESH_PASSWORD = "REFRESH_PASSWORD"
        const val INTERVAL_5 = 5
        const val INTERVAL_10 = 10
        const val INTERVAL_15 = 15
        const val INTERVAL_30 = 30
        const val INTERVAL_45 = 45
        const val INTERVAL_60 = 60

        /// User Data
        const val imagEnableEmailVisisbility = "imagEnableEmailVisisbility"
        const val USER_NAME ="USER_NAME"
        const val USER_PHONE ="USER_PHONE"
        const val COUNTRY_NAME ="COUNTRY_NAME"
        const val COUNTRY_CODE ="COUNTRY_CODE"
        const val USER_COMPANY_NAME ="USER_COMPANY_NAME"
        const val mySimpleSavedPassword = "simpleSavedPassword"
        const val isSavedEmail = "isSavedEmail"


        const val  INSTALL_TV_JSON_USER_CLICKED = "INSTALL_TV_JSON_USER_CLICKED"
        const val  installTVMode = "installTVMode"
        const val  hide_TV_Mode_Label = "hide_TV_Mode_Label"
        const val  installTVModeForFirstTime = "installTVModeForFirstTime"
        const val  fullScreen_APP = "fullScreen_APP"
        const val  hide_Full_ScreenLabel ="showFull_ScreenLabel"
        const val  immersive_Mode_APP = "immersive_Mode_APP"
        const val  hide_Immersive_ModeLabel = "show_Immersive_ModeLabel"
        const val  hide_BottomBar_APP = "show_BottomBar_APP"
        const val  hide_Bottom_Bar_Label_APP = "showBottom_BarLabel_APP"
        const val  hideBottom_MenuIcon_APP = "hideBottom_MenuIcon_APP"
        const val  hide_Bottom_MenuIconLabel_APP = "showBottom_MenuIconLabel_APP"
        const val  hide_Floating_Button_APP ="showFloating_Button_APP"
        const val  hide_Floating_ButtonLabel_APP = "showFloating_ButtonLabel_APP"
        const val  use_local_schedule_APP = "use_local_schedule_APP"
        const val  show_local_schedule_label = "show_local_schedule_label"



        // Move back to webviewpage
        const val MOVE_BK_WEBVIEW_TIME = 1L*90L*1000L



        // Init the Selected Sync Typess
        const val IMG_SELECTED_SYNC_METHOD = "IMG_SELECTED_SYNC_METHOD"
        const val USE_ZIP_SYNC = "USE_ZIP_SYNC"
        const val USE_API_SYNC = "USE_API_SYNC"
        const val USE_PARSING_SYNC = "USE_PARSING_SYNC"
        const val USE_DRIVE_SYNC = "USE_FTP_SYNC"

        /// for parsing sync service
        const val RECIVER_PROGRESS = "RECIVER_PROGRESS"
        const val RECIVER_DOWNLOAD_BYTES_PROGRESS = "RECIVER_DOWNLOAD_BYTES_PROGRESS"
        const val ParsingStatusSync = "ParsingStatusSync"
        const val ParsingProgressBar = "ParsingProgressBar"
        const val numberFailedFiles = "numberFailedFiles"
        const val ParsingDownloadBytesProgress = "ParsingDownloadBytesProgress"



        // Init the Selected Sync Typess
        const val IMG_TOGGLE_FOR_ORIENTATION = "IMG_TOGGLE_FOR_ORIENTATION"
        const val USE_POTRAIT = "USE_POTRAIT"
        const val USE_LANDSCAPE = "USE_LANDSCAPE"
        const val USE_UNSEPECIFIED = "USE_UNSEPECIFIED"


        // Temporay Load to web View page
        const val USE_TEMP_OFFLINE_WEB_VIEW_PAGE = "USE_TEMP_OFFLINE_WEB_VIEW_PAGE"

    }
}
