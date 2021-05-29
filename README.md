# Recipe Builder
A mobile app for making and storing recipes as a part of ITEC 4860 - Software Development Project course. Our team decided to take the unique challenge of making two apps simultaneously. This is the mobile app that I developed. The web application sits in a private repo owned by the instructor. Originally they had a single Firebase backend servicing things, but it has since been turned off.

The application is able to take down steps for a recipe using Android's speechRecognizer api. Additionally, it makes use of Android's "app actions", which enables booting the app and/or starting recording activity for the app via Google Assistant, which the user can leverage to take down a recipe without physically interacting with the device. At the time of development, Android's app actions were a feature in development preview. 

## Login
![login](docs/imgs/rbuilder-1.png)


## Authentication options
The login data was saved to Firestore, which would sync the login data across the web and mobile applications.

![auth](docs/imgs/rbuilder-2.png)


## Sample recipe list
Generated by RecipeUtil.kt using api data from themealdb.com, but the api has changed since then so the data is a little incomplete now when pulled by RecipeUtil.

![recipelist](docs/imgs/rbuilder-3.png)


## Sample recipe
Adding and deleting a recipe on mobile or web was synced so the lists were consistent. One could edit on one and the other would update accordingly.

![recipe](docs/imgs/rbuilder-4.png)
