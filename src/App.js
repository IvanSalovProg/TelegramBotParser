import { useState } from "react";
import { Slider } from "@mui/material";
import Layout from "./components/Layout/Layout";
import classes from "./index.module.css";

function App() {
  const [sliderValue, setSliderValue] = useState(50000);

  const changeSliderValueHandler = (event, value) => {
    setSliderValue(value);
  };

  console.log(sliderValue);
  return (
    <Layout>
      <div className={classes.filters}>
        <div className={classes.revenue}>
          <h3 className={classes["revenue-title"]}>Уровень дохода:</h3>
          <div className={classes["revenue-range"]}>
            <span>
              <b>От</b>:{" "}
            </span>
            <span>₽ {sliderValue}</span>
          </div>
          <Slider
            className={classes["revenue-slider"]}
            value={sliderValue}
            onChange={changeSliderValueHandler}
            max={300000}
            aria-label="Default"
            valueLabelDisplay="auto"
          />
        </div>
        <div className={classes.grade}>
          <h3 className={classes["grade-title"]}>Квалификация:</h3>
          <div className="checkbox">
            <label>
              <input type="checkbox" value="junior" />
              Junior
            </label>
          </div>
          <div className="checkbox">
            <label>
              <input type="checkbox" value="Middle" />
              Middle
            </label>
          </div>
          <div className="checkbox">
            <label>
              <input type="checkbox" value="Senior" />
              Senior
            </label>
          </div>
        </div>
        <div style={classes.areas}>
          <h3 className={classes["areas-title"]}>Специализация:</h3>
          <div className={classes["areas-btn-inner"]}>
            <button className={classes.btn}>JavaScript</button>
            <button className={classes.btn}>C#</button>
            <button className={classes.btn}>Java</button>
            <button className={classes.btn}>QA</button>
            <button className={classes.btn}>Python</button>
            <button className={classes.btn}>Data Science</button>
          </div>
        </div>
      </div>
    </Layout>
  );
}

export default App;
