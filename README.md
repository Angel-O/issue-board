# Note

Archiving. Reason being: this has evolved to become a bigger (private) project. Notable changes: migration to ZIO 2.0 & vite.js. Enjoy the archive & happy coding 🚀

# issue-board

ZIO/scala.js playground

## Requirements

- docker (needs to be up and running before launching the project)
- node.js/npm/yarn (recommended node version: `14.17.1`, the shell the project is launched from must use this version)
- sbt
- scala (depends on java8)
- `.env` file at the root of the front-end project (there's a `.env.sample` file in the folder already. 
  Just copy it and/or rename it to `.env`)

## Running modes

- Development mode (backend and front end will reload after each change)
- Docker
- Individual services
- A/B testing

#### 1. Development mode

Open a terminal window and type

```sbt dev```

- Ui is served at `localhost:12345` in reload mode
- Backend runs on `localhost:8080` in reload mode
- Dynamo db (via Docker) runs on `localhost:8000`

##### Notes

- if the sbt console is not shut down gracefully (e.g. with the `exit` command or by issuing `ctrl + D` on Mac) the 
dynamo db container will keep running. To kill it you can type the following command:
    `sbt killDb` or stop it and remove it using the docker-cli
- if you make changes affecting backend and frontend configuration (so `application.conf` and `application.yml` 
(e.g to change the server port)) you might have to rebuild the ui artifacts 
(the quickest way to do that is to add the clean command before the dev one. E.g. `sbt clean dev`)


#### 2. Docker

open a new terminal window and type the shell command

`sh local [options] [ui | backend | db | all]`

The UI will be served at `localhost:7000` via an nginx docker container. The `nginx` docker container will also 
serve as a proxy for the backend, which on this mode won't be bound to localhost (same for the dynamo db instance).
That means you won't be able to access them directly (unless you edit the docker-compose file) 

If you've specified the `ui` option (or `all`) when running the shell command, you will be able to see the 
`nginx` access logs on the terminal as you navigate to the page. (Note: not every page requires fetching data from the 
backend)

##### options

- `-b` Stands for "build backend" (will build the docker image for the backend project. It is recommended you run the 
script with the -b flag the very first time. For subsequent runs you can omit the `-b` option, unless you've made 
changes to the backend project: in that case you want to rebuild it.)

- `[ui | backend | db | all]` Indicates the name of the service whose logs will be tracked on the console; 
by default they will run in the background (with no logs printed out). 
The shell will be kept open displaying the logs of the service specified. You can also specify no 
service at all and no logs will be sent to the stdout).

##### notes

- it is recommended that you hit `ctrl + c` to shut down all services (rather than closing the terminal window). 
Clean up logic to shut down the docker environment will be executed following a `ctrl + c` signal.
- the docker-compose file used to run the services is generated from a template. It gets deleted on `ctrl + c`. 
If you need to make changes, edit the template rather than the generated file.
- the dynamodb data is persisted in a docker volume even after the services are shut down or the machine rebooted. This 
is to avoid having to recreate data all the time during development (yes we could have a script to generate data...). 
 If you want to get rid of the volume you'll have to manually remove it with the following command:
    `docker volume rm issue_board_data` (Alternatively you could restart the sbt shell and run the `exit` command, 
    or simply run `sbt exit`)


##### example usage

- `sh local -b ui`
- `sh local all`
- `sh local`


#### 3. Run each project individually

##### run backEnd

Type the following command on a terminal window

`sbt server` (this will run the server on `localhost:8080` and dynamodb (via Docker) on `localhost:8000`)


##### run frontEnd

Type the following command on another terminal window 

`sbt ui` (the ui will be served at `localhost:12345` in interactive mode (page will reload as you make changes 
to the code))


#### 4. A/B testing

You can even run services in parallel (docker mode vs dev mode). This can be useful for A/B (ui) testing.
To do so, you should run the `sh local` script first and then the `sbt dev` command on another terminal. This is because 
the `sh local` will clear the build directory which contains assets monitored by sbt when you run `sbt dev` (which uses
watch mode, or hot reload, or refresh mode...whatever you want to call it). If you invert the order of the commands
you will see lots of errors on the terminal where you run the `sbt dev` command.


So the workflow in this scenario would be:
1. run `sh local` on a terminal window (ui @ `localhost:7000`)
2. run `sbt dev` on a different terminal (ui @ `localhost:12345`)
3. make changes to the code
4. compare the new page @ `localhost:12345` vs the old page @ `localhost:7000` 


###### Useful Resources

- Scala.js: [docs](https://www.scala-js.org/)
- React via scala.js [scalajs-react](https://github.com/japgolly/scalajs-react)
- State management via [diode](https://github.com/suzaku-io/diode) (see also [gitBook](https://diode.suzaku.io/))
- ScalaCss (type-safe css!!) [scalacss](https://github.com/japgolly/scalacss) (see also [gitBook](https://japgolly.github.io/scalacss/book/))
- Bulma css framework [docs](https://bulma.io/) 
- Bundling npm dependencies and webpack config [scalajs-bundler](https://scalacenter.github.io/scalajs-bundler/)
- Asset management, config, development and more: [webpack docs](https://webpack.js.org/)
- Tooling: [workbench plugin](https://github.com/lihaoyi/workbench) - NO LONGER USED (replaced by webpack)
- Async & concurrent functional programming [zio](https://zio.dev/) & [cats-effect](https://typelevel.org/cats-effect/) - (cats-effect NOT CURRENTLY USED)
- Functional http server and client [http4s](https://http4s.org/)
- Create react app scala [g8 template](https://github.com/shadaj/create-react-scala-app.g8)  
