# SnapCalendar

This is an Android Application that allows the user to add an event to his/her calender by snapping a photo of a poster
with the event info.  Uses the Tesseract OCR engine.

Contributors:
- Calvin Giroud - eourcs
- Nirmal Krishnan - nkrishn9
- Jonathan Liu - jonl1096
- Nav Thakur - nthakur34


TODO:
- put tesseract processing in an AsyncTask
- add progressbar for tesseract processing
- fix issue that when app starts in portrait mode and picture is taken in landscape the file info gets messed up
  and program doesn't work
  - has something to do with when the program changes orientation, the activity is destroyed and recreated
- fix issue with tesseract not working well with varying text sizes within one photo
- parse text accurately
  - string matching
- finish calendar functionality
