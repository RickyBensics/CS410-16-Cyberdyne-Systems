# CTFastrak_Android_Application
The reason for this new repository is that some filepaths were over 240 characters which made the project uncompilable...
To run this program, download Android Studio, clone or download the git repository, open project and select "App" wherever you downloaded it, Build > Make Project, Run > Run 'app' (having an Android phone connected with developer options is recommended).

NEW in this iteration:
  * App icon and name created
  * Route highlighting implemented
  * Bus stops now display correct information
  * Calendar GTFS file implemented
  * App only shows routes and bus stops for the current day
  * Rudimentary search bar implemented (for bus stops)
  * Buses are displayed in realtime along selected route
  
TODO:
  * Add splash screen on startup
  * Make info box more robust (e.g display time until next arriving bus)
  * Replace default marker icons for bus stops and buses
  * Make bus stops selectable as either arrival or departure location from info box or search bar
  * Implement SearchView for search bar
  * Display and center initially on user location
  * Implement path finding between bus stops
