/**
 * Visiomov Android – VisioMove Essential (Visioglobe) integration
 *
 * @format
 */

import { StatusBar, StyleSheet, useColorScheme, View } from 'react-native';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { VisioMoveView } from './src/VisioMoveView';

// Replace with your own map hash from Visioglobe (see README)
const DEFAULT_MAP_HASH =
  'dev-m2ca47a978ce45fd3f3334b7a1078272ec07655bc';

function App() {
  const isDarkMode = useColorScheme() === 'dark';

  return (
    <SafeAreaProvider>
      <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />
      <View style={styles.container}>
        <VisioMoveView
          mapHash={DEFAULT_MAP_HASH}
          mapSecretCode={0}
          style={styles.map}
        />
      </View>
    </SafeAreaProvider>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  map: {
    flex: 1,
  },
});

export default App;
