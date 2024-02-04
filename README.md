# maps-dsedarou-felia
Project description --
Project name: Maps
Team members and contributions: felia and dsedarou
Total estimated time it took to complete project: 20 hours
A link to your repo: https://github.com/danielsedarous/Maps.git

Design choices -- 
Explain the relationships between classes/interfaces: Our MapBox and Overlay classes interact to visually display the map and its functionalities on the page (highlighting the redlining data and highlighting areas that match the user's key word search). Our map also requires the ability to query our backend server, specifically, our MapsAreaKeywordHandler and MapsBoundingBoxHandler endpoints. Our Map originally loads in its data through a bounding box query of the entire world, and then queries our area keyword handler to highlight specific areas on the map. We used our structure from the REPL sprint in order to implement a command line box that allows the user to search for either a specific area, or for the broadband percentage of a given state and county (done by querying our backend BroadbandHandler). This is accomplished through the interaction of REPL, REPLHistory, REPLInput, and ControlledInput which take in the users input and set/clear the result history accordingly.
Discuss any specific data structures you used, why you created it, and other high level explanations: We used a map to store response data in the backend such that we could easily access it from the front end and use the error handling logic from the backend instead of reimplementing it in the frontend. We also used a map to store our search history in the backend MapAreaKeyWordHandler class such that the area description was mapped to a feature collection of features that contain that description because we thought that a map made the most sense to have some search history persist on the backend given that the search and the results had to be associated in some way.

Errors/Bugs: N/A
Explanations for checkstyle errors: N/A

Tests -- 
Explain the testing suites that you implemented for your program and how each test ensures that a part of the program works:
We test for the following events in our front and back end- these test both individual functionalities as well as their interactions within the program.
Frontend:
- Tests that the command input bar is visible on the page.
- Tests that the history box is visible on the page.
- Tests that the map is visible up on the page
- Tests that the text in the command input bar changes after typing in some input.
- Tests basic broadband querying result
- Tests that an error message is appropriately displayed when a state and county that do not have census data are inputted by the user.
- Tests that an informative and accurate error message is displayed to the user when an invalid number of arguments are inputted after the "broadband" command.
- Tests that page accurately displays a success statement when a given area is highlighted on the map
- Tests that a desctiptive error message is returned when there are no results for a given keyword search
- Tests that an informative and accurate error message is displayed to the user when an invalid number of arguments are inputted after the "highlight" command.
- Tests that if the user does not input "broadband" or "highlight" as the first word of their command it will retun a desctiptive error message
- Tests the interaction of broadband and highlight searches, both successful and not successfuly
Backend:
    Broadband:
    - Tests broadband connection
    - Tests broadband success with mocked data
    - Tests broadband failure with mocked data
    - Tests broadband success with real data
    - Tests broadband failure with read data
    - Tests state and county codes helper method
    - Tests when a county is not in given state
    Area:
    - Tests no area parameter given
    - Tests search history persists
    - Tests no area in dataset
    - Tests no keyword provided
    - Tests successful search
    - Tests successful search on different city
    Bounding Box:
    - Tests no parameter given
    - Tests empty parameters given
    - Tests successful response
    - Tests invalid values for all 4 parameters
    - Tests out of range values for all 4 parameters
    - Fuzz tests random ints generated for parameters

How to… --
Run the tests you wrote/were provided: For frontend, to run the tests we wrote in the App.spec.ts file, cd into the maps directory and then the frontend drectory, then run "npx playwright test" from the terminal. For backend, navigate to one of our testing files and press the green play button at the top.
Build and run your program: To build and run our program, cd into the maps directory and then the frontend drectory, then run "npm run dev" from the terminal. This should output a url that you can copy into your browser which should load our page. It will display a search bar and a map highlighting redlining data in red. You can input either of the following commands into the search bar and then press the enter key to submit: broadband <state> <county> (will return the broadband percentage for that county) or highlight <area key word description> (will highlight areas with that keyword on the map in grey). Note that if you are inputting words seperated by spaces as a state for example, Rhode Island, you should surround the term by carrot brackets ("broadband <Rhode Island> <Providence County>"). You will also have to create your own api key and store it in api.ts within the private folder.


Whose Labor?
Your finished Maps product is built using many systems: programming languages, development environments, software packages, hardware, etc.  Whose labor do you rely on when you run your capstone demo? Enumerate at least 12 different packages, tools, hardware components, etc. that you implicitly or explicitly used during this week’s work. 
1. IntelliJ: Hosts our backend
2. VS Code: Hosts our frontend
3. Java: coding laguage used in the backend
4. TypeScript: coding language used in the frontend
5. Playwright: allows us to test our code in the frontend
6. J-unit: allows us to test our code in the backend
7. Moshi: used to parse files and move from and to JSON files
8. React: allows us to use state functionality to manipulate variables
9. MapBox: allows program to display an interactive map
10. National Weather Service API: source of our broadband data
11. spark.java: allows us to handle requests/responses and user interaction
12. Keyboard Events and Event Listeners: allows use to handle key inputs for accessibility
13. HTML/CSS: allows us to create visual aspects of our program
