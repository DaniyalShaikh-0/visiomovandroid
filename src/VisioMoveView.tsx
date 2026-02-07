/**
 * React Native wrapper for VisioMove Essential (Android native bridge).
 * Renders the Visioglobe map view when mapHash and optional mapSecretCode are provided.
 * On iOS this component renders a placeholder (native bridge is Android-only).
 *
 * @see https://my.visioglobe.com/docs/VisioMoveEssential-Android-V2/VisioMoveEssential-Android/
 */

import type { ViewProps } from 'react-native';
import {
  Platform,
  requireNativeComponent,
  StyleSheet,
  Text,
  View,
} from 'react-native';

export interface VisioMoveViewProps extends ViewProps {
  /** Online map hash from Visioglobe (required to load the map). */
  mapHash: string;
  /** Map secret code (default 0). */
  mapSecretCode?: number;
}

const NativeVisioMoveView =
  Platform.OS === 'android'
    ? requireNativeComponent<VisioMoveViewProps>('VisioMoveView')
    : null;

export function VisioMoveView({
  mapHash,
  mapSecretCode = 0,
  style,
  ...rest
}: VisioMoveViewProps) {
  if (Platform.OS !== 'android' || NativeVisioMoveView == null) {
    return (
      <View style={[styles.container, styles.placeholder, style]} {...rest}>
        <Text style={styles.placeholderText}>
          VisioMove Essential is available on Android only.
        </Text>
      </View>
    );
  }

  return (
    <View style={[styles.container, style]} {...rest}>
      <NativeVisioMoveView
        mapHash={mapHash}
        mapSecretCode={mapSecretCode}
        style={StyleSheet.absoluteFillObject}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    minHeight: 200,
  },
  placeholder: {
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#f0f0f0',
  },
  placeholderText: {
    color: '#666',
    fontSize: 14,
  },
});
