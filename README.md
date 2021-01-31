## Inspiration
A Pokedex is a small, portable device that can detect any Pokemon it sees and return its description and data via speech. In this 24-hour hackathon, we turned that impossible dream into reality by building our mobile app, REALDex, the Real Pokedex.

## What it does
The REALDex is an image classifying mobile app that detects Pokémon from an uploaded image or camera. When it detects a Pokémon, it will fetch that Pokémon’s data from PokeAPI then return the Pokémon’s data via speech, just the Pokedex we see on TV shows!

> See Devpost link: https://devpost.com/software/realdex

## How we built it
Using Google Cloud technologies (AutoML Vision, Storage and Text-To-Speech), we built a custom image classification model using over 10000 images for over 150 species of Pokémon.

> Disclaimer: The model we built was prepared before the start of this hackathon.

In this short 24-hour hackathon, we took our pre-built custom model and integrate it into our app using Firebase ML Kit and Android Studio. The mobile app is written in Kotlin, and it was our first time writing in that language.

## Challenges we ran into
- Importing a large model and compressing it for mobile use took some time to figure out.
- First time trying to use Android Studio and Firebase ML Kit, we spent a lot of time researching how to allow the app to use the camera to detect Pokémon.
- Android Studio: Took a lot of time navigating how to use and create a basic UI for the app. Researched third-party libraries such as CameraX and Text-to-Speech to include all the features we want for this app.
- The Text-to-Speech pronounced some Pokémon names incorrectly and also pronounced Pokemon as “Poke-kuh-man” so we had to specifically tell it to pronounce as “Pok-kay-mon” as a workaround.
- First time writing in Kotlin. Android Studio uses Java or Kotlin, and we decided to write in Kotlin to learn the language and challenge ourselves for this hackathon.

## Accomplishments that we're proud of
- Completed a basic Pokedex app which works very similarly to the ‘real’ one from our childhood
- Achieved an average 80% accuracy with the compressed model. It can also detect live Pokémon objects, not just images from Google.

## What we’ve learned
- A great deal about Android Studio in a short amount of time
- Basic Kotlin knowledge acquired!
- Google Cloud products such as AutoML Vision, Storage and Text-to-Speech that can turn an impossible dream into reality
- Extensions of Google Cloud products like Firebase ML Kit

## What's next for REALDex
- More Pokémon: there over 900 different species waiting to be included in the AutoML Vision Image Classification model.
- Higher Accuracy: we plan to achieve over 95% accuracy as we continue to improve the custom model.
- Animations & UI: we definitely need to improve the look and feel of the app to make it seem more like a real Pokedex. We plan to start researching into Android Studio animations and charting libraries, so that the app not only returns the Pokémon’s data via speech, but also displays its information in visually appealing charts.

## Screenshots
<img src="https://res.cloudinary.com/viclo2606/image/upload/v1612095169/devpost/2_citbi1.png" width="500px"/>

<img src="https://res.cloudinary.com/viclo2606/image/upload/v1612095169/devpost/1_xgv6xa.png" width="500px"/>

<img src="https://res.cloudinary.com/viclo2606/image/upload/v1612095169/devpost/3_aigzsu.png" width="500px"/>
