# homem-luzula - field data collecting app, for species inventories

## Who is this app for?

**Field botanists** who can identify most of the plant species *de visu*.

## What is this app for?

For conducting **floristic surveys** (species inventories) **as fast as you can**.

## But how?

This app is optimized for **greatly speeding up** and simplifying **field data collection**, in the form of species lists with
ID, abundance, phenological state and other data.

Species are recorded by tapping its abbreviation (4 letters or less). The abbreviation is looked up in the user
defined checklist and matching species are displayed for selection. For example, "sarv" will match *Stachys arvensis*,
*Sinapis arvensis*, among a few others, depending on the reference checklist.
Phenological state and ID uncertainty can be directly specified with one tap.

Further, you can **pin some species in a quick access toolbar**, so you can record their occurrence (species, date and coordinates)
with a single tap. This is useful when you're conducting a fine scale occurrence map of a few species.

Full **GPS navigation over aerial imagery** is integrated, and **GIS vector layers can be added** (e.g. for displaying
the limits of the study area, a grid, etc.).

Data is output as CSV text tables, that can easily be uploaded to any database. Tracklog is exported in GeoJson format,
compatible with GIS software.

## What is this app not for?

**Not for** identifying species in the field. This app assumes you already know the species that you are inventorying.
This app is for serious field work at high speed.

## Highlights
* Fast inventorying by looking up species short abbreviations
* Fast mark pinned species
* Full GPS navigation over aerial imagery (using [osmdroid](https://github.com/osmdroid/osmdroid))
* Automatically stores aerial imagery for offline use
* Records the tracklog
* Imports vector layers from GIS
* Very easy handling of vector layers


