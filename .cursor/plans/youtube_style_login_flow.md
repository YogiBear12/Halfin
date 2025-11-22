# YouTube-Style Login Flow

## Overview
Redesign the user login flow to match YouTube's login screen with a split-screen layout. Left side contains two sign-in options, right side displays dynamic content based on which option is focused.

## Requirements

### Layout Structure
- **Fullscreen layout** (replaces current dialog)
- **Left side (40-50% width)**: Two sign-in option buttons
  - "Sign in with your phone" (top)
  - "Sign in with your remote" (bottom)
- **Right side (50-60% width)**: Dynamic content area that updates based on focus

### Sign in with Phone Flow
When "Sign in with your phone" is focused/selected:
- Display QR code containing Quick Connect URL: `{{server-url}}/web/#/quickconnect?code={{quick-connect-code}}`
- Show instructions: "Scan QR code or go to yt.be/activate" (but use Jellyfin URL)
- Display Quick Connect code prominently (e.g., "WVC-NKV-XDG")
- Automatically initiate Quick Connect when this option is focused
- Poll for authentication status (existing logic in ViewModel)

### Sign in with Remote Flow
When "Sign in with your remote" is focused/selected:
- Display username and password input fields
- Use existing login logic from ViewModel
- Show error messages if login fails

## Implementation Plan

### 1. Add QR Code Library
**File**: `gradle/libs.versions.toml` and `app/build.gradle.kts`
- Add zxing-core library for QR code generation
- Add zxing-compose library for rendering QR codes in Compose

### 2. Create New Login Screen Composable
**File**: `app/src/main/java/com/github/damontecres/wholphin/ui/setup/LoginScreen.kt` (new file)
- Create `LoginScreen` composable with split layout
- Left column with two buttons using `Card` or `Button` components
- Right side `Box` that switches content based on selected option
- Use `interactionSource.collectIsFocusedAsState()` to track which button is focused
- Handle focus changes to update right side content

### 3. Implement Phone Sign-In Content
**In**: `LoginScreen.kt`
- Create `PhoneSignInContent` composable
- Generate QR code bitmap from URL: `"${server.url}/web/#/quickconnect?code=${quickConnect.code}"`
- Display QR code using Compose QR code library
- Show Quick Connect code text below QR code
- Show instructions text
- Handle loading state while Quick Connect code is being generated

### 4. Implement Remote Sign-In Content
**In**: `LoginScreen.kt`
- Create `RemoteSignInContent` composable
- Reuse existing username/password input fields from current dialog
- Use `EditTextBox` components
- Handle form submission and error display

### 5. Update SwitchUserContent
**File**: `app/src/main/java/com/github/damontecres/wholphin/ui/setup/SwitchUserContent.kt`
- Replace `BasicDialog` with new `LoginScreen` composable when `showAddUser` is true
- Remove dialog wrapper
- Make login screen fullscreen
- Handle back button to close login screen

### 6. Update ViewModel (if needed)
**File**: `app/src/main/java/com/github/damontecres/wholphin/ui/setup/SwitchUserViewModel.kt`
- Ensure `initiateQuickConnect()` is called when phone option is focused
- May need to add method to cancel Quick Connect when switching to remote option
- Ensure Quick Connect polling continues while phone option is active

### 7. Add String Resources
**File**: `app/src/main/res/values/strings.xml`
- Add `sign_in_with_phone`: "Sign in with your phone"
- Add `sign_in_with_remote`: "Sign in with your remote"
- Add `scan_qr_code_or_go_to`: "Scan QR code or go to"
- Add `enter_code`: "Enter the code"

## Technical Details

### QR Code Generation
- Use zxing library to generate QR code bitmap
- URL format: `"${server.url}/web/#/quickconnect?code=${quickConnect.code}"`
- QR code should be large enough to scan easily (e.g., 300-400dp)

### Focus Management
- Use `FocusRequester` for initial focus on "Sign in with your phone"
- Track focus state using `interactionSource.collectIsFocusedAsState()`
- Update right side content when focus changes using `LaunchedEffect(focused)`

### Quick Connect Flow
- Generate Quick Connect when "Add User" button is pressed (in UserList)
- Store Quick Connect state in ViewModel
- When phone option is focused, display existing Quick Connect code/QR
- Poll for authentication status (existing logic)
- When authenticated, navigate to home (existing logic)
- Do NOT regenerate Quick Connect when switching between options

### Layout Structure
```kotlin
Box(modifier = Modifier.fillMaxSize()) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Left side - Sign-in options
        Column(modifier = Modifier.weight(0.4f)) {
            PhoneSignInButton(...)
            RemoteSignInButton(...)
        }
        // Right side - Dynamic content
        Box(modifier = Modifier.weight(0.6f)) {
            when (selectedOption) {
                SignInOption.PHONE -> PhoneSignInContent(...)
                SignInOption.REMOTE -> RemoteSignInContent(...)
            }
        }
    }
}
```

## Files to Create/Modify

1. **New**: `app/src/main/java/com/github/damontecres/wholphin/ui/setup/LoginScreen.kt`
2. **Modify**: `app/src/main/java/com/github/damontecres/wholphin/ui/setup/SwitchUserContent.kt`
3. **Modify**: `gradle/libs.versions.toml` (add zxing library)
4. **Modify**: `app/build.gradle.kts` (add zxing dependency)
5. **Modify**: `app/src/main/res/values/strings.xml` (add new strings)

## Dependencies
- zxing-core for QR code generation
- zxing-compose (or similar) for rendering QR codes in Compose
- Existing Quick Connect API from Jellyfin SDK
- Existing login API from Jellyfin SDK

