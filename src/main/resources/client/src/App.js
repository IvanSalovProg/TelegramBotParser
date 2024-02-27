function App() {

  const onClickHandler =()=> {
    alert("Hello")
  }

  return (
    <div className="App">
      <button onClick={onClickHandler}>Click me</button>
    </div>
  );
}

export default App;
