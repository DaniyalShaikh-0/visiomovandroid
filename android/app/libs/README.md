# VisioMove Essential AAR

Place **VisioMoveEssential.aar** in this folder before building the Android app.

## How to obtain VisioMoveEssential.aar

The AAR is **not** publicly downloadable. You get it through Visioglobe:

1. **From your Visioglobe project**  
   If you already have a Visioglobe project (e.g. for map authoring or another platform), ask your **Visioglobe project manager** for the **VisioMove Essential Android SDK**. They typically provide a package that includes `VisioMoveEssential.aar` and sometimes sample apps (“SDK folder” mentioned in the [Getting started](https://my.visioglobe.com/docs/VisioMoveEssential-Android-V2/VisioMoveEssential-Android/GettingStarted.html) guide).

2. **Developer portal (my.visioglobe.com)**  
   Log in at [my.visioglobe.com](https://my.visioglobe.com) with the credentials your project manager gave you. Check your project’s resources or downloads for the Android SDK / AAR. Map hash codes are available under the **Targets** tab.

3. **New project or no access yet**  
   Contact Visioglobe to get SDK access and the AAR:  
   [visioglobe.com/contact](https://visioglobe.com/contact) — “Talk to experts”.

After you have the file:

- Copy **VisioMoveEssential.aar** into this `android/app/libs/` directory.
- Rebuild: `npm run android` or `yarn android`.

Without the AAR, the Android build will fail when compiling the VisioMove native bridge.
