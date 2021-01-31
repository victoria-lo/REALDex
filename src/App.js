import React, { useState, useEffect } from "react";
import * as automl from "@tensorflow/tfjs-automl";
import * as tf from '@tensorflow/tfjs'
import './App.css'; 

function App(){
  useEffect(()=>{
    main();
  },[])

  tf.setBackend("cpu");
  const [image, setImage] = useState(null);

  const loadDictionary = () => {
    fetch('./dict.txt')
    .then((r) => r.text())
    .then(text  => {     
      text.trim().split("\n");
      console.log(text);
      return text;
    })  
  };

  const loadImageClassification = async (modelUrl) => {
    const [model, dict] = await Promise.all([tf.loadGraphModel(modelUrl),
    loadDictionary()
    ]);
    return new automl.ImageClassificationModel(model, dict);
  };


  const main = async () => {
    const modelURL = "https://storage.googleapis.com/pokemon-tflite/model-export/icn/tf_js-pokemon_gen_1-2020-12-21T10%3A52%3A49.936335Z/model.json";
    const model = await loadImageClassification(modelURL);
    return await model.classify(image);
   
  };
    
  
    const handleUpload = event => {
      const { files } = event.target;
      if (files.length > 0) {
        const url = URL.createObjectURL(event.target.files[0]);
        setImage(url);
      }
    };

    return(
      <div>
        <input
        type="file"
        accept="image/*"
        onChange={handleUpload}
      />
      </div>
    )
  }

  

export default App;
