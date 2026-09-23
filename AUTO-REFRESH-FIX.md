# MAHA E-HOUSING — Automatic Dashboard Refresh

The customer, seller, and admin dashboards now automatically synchronize with the backend without requiring logout/login or manual page refresh.

## Behavior
- Customer data refreshes every 5 seconds while the tab is visible.
- Seller data refreshes every 5 seconds while the tab is visible.
- Admin data refreshes every 5 seconds while the tab is visible.
- House listings refresh every 5 seconds.
- Returning to the tab or window focus triggers an immediate refresh.
- The selected house details are updated when fresh house data arrives.
- Authentication/session is not cleared by refresh.

## Why
This lets changes made by one role appear in another role's dashboard shortly after they are saved:
- Seller publishes a house -> customer sees it automatically.
- Customer books -> seller sees the booking automatically.
- Seller approves -> customer sees the updated booking automatically.
- Customer submits payment -> seller sees the pending payment automatically.
- Seller confirms payment -> customer sees RECEIVED, balance, receipt and contract automatically.
- Admin changes seller/customer/house data -> admin lists refresh automatically.

No logout/login is required.
