# MarkdownResolver

Based on Koltin to resovle the Markdown file，and show it by TextView.Using spannable to improve performance by stitching text.

# How to use it
Now only one way to start it is local Markdown file.
```
Markdown.parser("filePath")
```
The resolver will return spannable to let TextView show the content.

But one thing you should pay attention to is that if you want to add a link，you should add the following code.
```
TextView.movementMethod = LinkMovementMethod.getInstance()
```