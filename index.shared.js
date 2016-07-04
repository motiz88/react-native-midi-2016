/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */
import 'es6-symbol/implement';
import React, { Component } from 'react';
import {
  AppRegistry,
  StyleSheet,
  Text,
  View
} from 'react-native';

import './webmidi/polyfill';

const MidiPort = ({id, port}) => <Text>{id}: {port.name} {port.manufacturer} {port.description} {port.version} {port.connection} {port.state}</Text>;
MidiPort.propTypes = {
  id: React.PropTypes.string.isRequired,
  port: React.PropTypes.object.isRequired
};
const MidiPorts = ({ports}) => ports ? <View>{[...ports.entries()].map(([id, port], i) => <MidiPort key={i} id={id} port={port} />)}</View> : null;
MidiPorts.propTypes = {
  ports: React.PropTypes.object.isRequired
};
class ReactNativeWebMidiApi extends Component {
  state = {
    inputs: new Map(), outputs: new Map(), midiMessages: []
  }
  componentDidMount () {
    this.initializeMidi();
  }
  handleClick = () => {
    this.initializeMidi();
  }

  handleMidiMessage = port => (ev) => {
    const arr = [];
    for (let i = 0; i < ev.data.length; i++) {
      arr.push((ev.data[i] < 16 ? '0' : '') + ev.data[i].toString(16));
    }
    const description = `${port.id}: ${arr.join(' ')}`;
    this.setState({midiMessages: [...this.state.midiMessages, description].slice(-10)});
  }

  async initializeMidi () {
    const midi = await navigator.requestMIDIAccess();
    const updateDevices = (port) => {
      const { inputs, outputs } = midi;
      inputs.forEach(port => {
        port.onmidimessage = this.handleMidiMessage(port);
      });
      this.setState({ inputs, outputs });
    };
    midi.onstatechange = updateDevices;
    updateDevices();
  }
  render () {
    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>
          Welcome to React Native MIDI!
        </Text>
        <Text>Inputs</Text>
         <MidiPorts ports={this.state.inputs} />
         <Text>Outputs</Text>
         <MidiPorts ports={this.state.outputs} />
        {this.state.midiMessages.map((m, i) => <Text key={i}>{m}</Text>)}
        <Text style={styles.instructions}>
          To get started, edit index.android.js
        </Text>
        <Text style={styles.instructions}>
          Shake or press menu button for dev menu
        </Text>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF'
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5
  }
});

AppRegistry.registerComponent('ReactNativeWebMidiApi', () => ReactNativeWebMidiApi);
