# HVL TimeEdit .ics simplifier

---
By: Martin Berg Alstad

## Before / after

TODO: Add images

## Usage

### Create .ics file from timeedit url

```bash
# Replace url with your own
url=https://cloud.timeedit.net/hvl/web/studbergen/ri6305Q64k59u6QZQtQn270QZQ8QY43dZ6317Z0y6580CwtZ00AZ87D9690F55D7EAEBF27863FFDA6.ics
curl -o -X PUT https://api.martials.no/hvl_ics_simplifier/create -H "Content-Type: text/plain" -d "$url"
```

### Get improved .ics file

```bash
# Replace with response from create
file=ri6305Q64k59u6QZQtQn270QZQ8QY43dZ6317Z0y6580CwtZ00AZ87D9690F55D7EAEBF27863FFDA6.ics
# To see the file
curl https://api.martials.no/hvl_ics_simplifier/get/$file
# To download the file
wget https://api.martials.no/hvl_ics_simplifier/get/$file
```