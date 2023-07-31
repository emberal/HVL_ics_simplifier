# HVL TimeEdit .ics simplifier

By: Martin Berg Alstad

## Before / after

Before:
![Before](src/main/resources/images/Calendar%20before.png)
After:
![After](src/main/resources/images/Calendar%20after.png)

## Usage

### Create .ics file from timeedit url

```bash
# Replace url with your own
url=https://cloud.timeedit.net/hvl/web/studbergen/ri6305Q64k59u6QZQtQn270QZQ8QY43dZ6317Z0y6580CwtZ00AZ87D9690F55D7EAEBF27863FFDA6.ics
curl -o --location --request PUT 'https://api.martials.no/hvl_ics_simplifier/create' \
--header 'Content-Type: text/plain' \
--data $url
```

### Get improved .ics file

```bash
# Replace with response from create
file=ri6305Q64k59u6QZQtQn270QZQ8QY43dZ6317Z0y6580CwtZ00AZ87D9690F55D7EAEBF27863FFDA6.ics
# To see the file
curl https://api.martials.no/hvl_ics_simplifier/ics/$file
# To download the file
wget https://api.martials.no/hvl_ics_simplifier/ics/$file
```

### Create and get improved .ics file

```bash
path=cloud.timeedit.net/hvl/web/studbergen/ri6305Q64k59u6QZQtQn270QZQ8QY43dZ6317Z0y6580CwtZ00AZ87D9690F55D7EAEBF27863FFDA6.ics
curl -o https://api.martials.no/hvl_ics_simplifier/$path
```