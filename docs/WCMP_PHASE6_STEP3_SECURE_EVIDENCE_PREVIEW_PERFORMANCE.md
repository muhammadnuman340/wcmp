# WCMP Phase 6 Step 3 - Secure Evidence Preview Performance & Adaptive Media Rendering

## 1. Implementation
- **Secure Image Downsampling:** Created a clean `SecureImageDecoder` abstraction. The default implementation (`DefaultSecureImageDecoder`) uses a two-pass `BitmapFactory` operation (`inJustDecodeBounds`) over `Dispatchers.IO` to intelligently downsample high-resolution images, safeguarding UI memory constraints dynamically based on layout bounds.
- **IO-thread Decoding:** Ensured all disk I/O occurs entirely on an isolated IO coroutine.
- **Adaptive Preview Layout:** The viewing environment now responsively bridges three distinct visual profiles using `WindowSizeClass`. The UI scales intelligently across Compact, Medium, and Expanded panes.
- **Audio Lifecycle:** Maintains explicit constraints ensuring audio rendering does not escape expected media lifecycle parameters.
- **Cancellation:** Actively aborts image and audio preparation upon ViewModel disposal.
- **Temporary-File Cleanup:** Decrypted, controlled temporary files are strictly bound and explicitly deleted whenever `EvidencePreviewViewModel` terminates or when a user closes the evidence. 

## 2. Test Architecture
- **Why Android framework mocks caused JVM failures:** Mocking raw classes like `Context`, `Bitmap`, and `Uri` inside standard JVM run configurations caused unresolvable `IllegalArgumentException` or stub crashes due to Mockito's inherent inline restrictions running against the un-instrumented android.jar.
- **Project-Owned Abstractions:** Introduced `PreviewImageData` and `EvidenceDirectoryProvider`.
- **JVM Tests:** These tests strictly assert state boundaries, lifecycle behaviors, fallback conditions, and logic gates via `FakePreviewImageData` and `AndroidEvidenceDirectoryProvider`. 
- **Robolectric/Instrumented Constraints:** Native media rendering elements (like `MediaPlayer` usage and real `Bitmap` configurations) correctly defer out of standard generic unit execution.

## 3. Security
- **Privacy Maintained:** Decrypted media strictly confines to an internal cache-only node.
- **No External Storage & Sharing:** Deliberately maintained exclusion of MediaStore, FileProvider, or export features.
- **No Disk Image Cache:** Purposely evaded tools like Coil to avoid hidden persistent cache generation.
- **Temporary File Cleansing:** Verification checks confirm files explicitly wipe from `/cache/controlled_viewing`. 
- **Key Unavailable / Compromised Handling:** Fallbacks stay pristine.

## 4. Verification
Executed tasks exactly as specified:
- `./gradlew clean`
- `./gradlew test`
- `./gradlew testDebugUnitTest`
- `./gradlew assembleDebug`
- `./gradlew lint`

**Final Status:** COMPLETED
