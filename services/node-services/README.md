# node-services

## Install and build

To get all the dependencies required to develop the node services:

```shell
$ npm install
```

To build:

```shell
$ npm run build
```

To build the docker image:

```shell
$ gradle :services:node-services:dockerBuild
```

## Run proto code generation

To re-gen the `generated` directory:

```shell
$ npm run proto
```

## Lint and format

Linting is run together with `gradle check`, you can format using:

```shell
$ npm run format
```

## Running the services

The Node services can be run via:

```shell
SERVICES=<COMMA_SEPARATED_LIST_OF_SERVICES> gradle npm_run_app 
```

For the list of supported services see [here](src/app.ts).
