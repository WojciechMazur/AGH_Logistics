import React, {Component} from 'react';
import './App.css';
import Dashboard from "./pages/transportDistribution/Dashboard";
import {library} from '@fortawesome/fontawesome-svg-core'
import {faEdit, faMinus, faPlus, faSave} from '@fortawesome/free-solid-svg-icons'

library.add(faPlus, faMinus, faSave, faEdit);

class App extends Component {
  render() {
    return (
      <Dashboard/>
    );
  }
}

export default App;
