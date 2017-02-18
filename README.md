Dropwizard Upload Lab
=

Simple toy-project to show how to upload files to a Dropwizard service via ReactJS.

## What it does

Demonstrates several things

0. How to handle file uploads in Dropwizard
1. Re-usable components with React
2. File upload without Authentication
3. File upload with token-based authentication (Json Web Tokens)
4. Refreshing/re-rendering ReactJS components (naively implemented)

## How to run it

I assume you have latest version of Maven and Java JDK 8 on your **PATH**

```bash
$ mvn clean package

$ java -jar target\dropwizard-upload-lab-1.0-SNAPSHOT.jar server dropwizard_upload.yml
```

And then go to ```localhost:9000``` in your browser. You should get a really badly styled
page with two "upload forms" - one requiring auth and another not.

On the upload form requiring authentication you can use

<pre>
  username: john
  password: john123
</pre>

or

<pre>
  username: foo
  password: bar
</pre>

## Notes

* This was done to learn basic ReactJS usage

* Using ```XHR``` without jQuery and should work on Firefox >= 30, haven't tested in other browsers .. oops :/

* The code for the React application is in one file, for simplicity [here](src/main/resources/webapp/application.js)

* No test coverage, also for simplicty - if you feel uncomfortable looking at codebase without
tests feel free to add them and send a PR ;P
