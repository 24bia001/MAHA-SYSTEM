# Receipt & Contract PDF download fix

The Receipt and Contract download buttons now generate a real PDF directly in the browser using a built-in PDF writer. No html2pdf CDN, external script, or HTML attachment is required.

- Downloaded files use `.pdf` extension.
- No HTML file is downloaded.
- Includes MAHA E-HOUSING branding, watermark text, property/payment/customer/seller information and signature lines.
- Existing booking, payment, authentication and auto-refresh functionality is unchanged.
- No database migration is required.
