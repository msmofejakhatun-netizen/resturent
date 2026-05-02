# Restaurant Captain Billing App

Native Android Kotlin app for fast restaurant captain billing with Bluetooth ESC/POS thermal printing.

## Features

- Kotlin Android app with Material Design UI
- Restaurant header with table number and auto-filled captain name
- RecyclerView item selection grouped by Roti, Naan, Sabji, and Drinks
- RecyclerView selected bill table with item, quantity, price, and total
- Automatic subtotal, CGST 2.5%, SGST 2.5%, round off, and grand total
- Bluetooth printer permission handling for Android 12+
- Paired printer detection, preferring Epson/POS/thermal device names
- ESC/POS formatted receipt using `[L]`, `[C]`, `[R]`, and `<b>` tags
- Save last bill and reprint last bill
- Day/night theme support

## Open in Android Studio

Open this folder as a project:

`RestaurantCaptainBillingApp`

Android Studio will sync dependencies from Google Maven, Maven Central, and JitPack. Pair the Epson Bluetooth printer in Android system settings before using Print Bill.
