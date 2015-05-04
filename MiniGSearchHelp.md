# Introduction #

As the extended search tool for minig is similar to the gmail's one, you can see the original help :
http://mail.google.com/support/bin/answer.py?answer=7190
(but there is some light differences)

# Details #

|  Operator | Definition | Example(s) |
|:----------|:-----------|:-----------|
|  from: | Used to specify the sender |   Example - <strong> from:amy</strong><br> Meaning - Messages from Amy <br>
<tr><td>  to: </td><td>  Used to specify a recipient </td><td>   Example - <strong>to:david</strong><br> Meaning - All messages that were sent to David (by you or someone else) </td></tr>
<tr><td>  subject:  </td><td>    Search for words in the subject line  </td><td>   Example - <strong>subject:dinner</strong><br> Meaning - Messages that have the word "dinner" in the subject </td></tr>
<tr><td>  OR  </td><td>    Search for messages matching term A or term B<br> <b>OR must be in all caps</b></td><td>   Example - <strong>(from:amy OR from:david)</strong><br> Meaning - Messages from Amy or from David, parenthesis MUST be used to get the correct result </td></tr>
<tr><td> - <br> (hyphen) </td><td>    Used to exclude messages from your search  </td><td>   Example - <strong>dinner -movie</strong><br> Meaning - Messages that contain the word "dinner" but do not contain the word "movie" </td></tr>
<tr><td>  has:attachment<br>  </td><td>   Search for messages with an attachment </td><td>   Example - <strong> from:david has:attachment </strong><br> Meaning - Messages from David that have an attachment </td></tr>
<tr><td>  has:invitation<br>  </td><td>   Search for messages with an event invitation </td><td>   Example - <strong> from:david has:invitation </strong><br> Meaning - Search for messages where david invites you to an event </td></tr>
<tr><td>  filename: </td><td>    Search for an attachment by name or type </td><td>   Example - <strong> filename:physicshomework.txt</strong><br> Meaning -  Messages with an attachment named "physicshomework.txt" Example - <strong> label:work filename:pdf</strong><br> Meaning -  Messages labeled "work" that also have a PDF file as an attachment </td></tr>
<tr><td> " " (quotes) </td><td>    Used to search for an exact phrase<br> <b>Capitalization isn't taken into consideration</b></td><td>   Example - <strong> "i'm feeling lucky"</strong><br> Meaning -  Messages containing the phrase "i'm feeling lucky" or "I'm feeling lucky" Example - <strong> subject:"dinner and a movie"</strong><br> Meaning -  Messages containing the phrase "dinner and a movie" in the subject  </td></tr>
<tr><td>  ( )<br>  </td><td>    Used to group words <br> Used to specify terms that shouldn't be excluded </td><td>   Example - <strong> from:amy(dinner OR movie)</strong><br> Meaning -  Messages from Amy that contain either the word "dinner" or the word "movie" Example - <strong> subject:(dinner movie)</strong><br> Meaning -  Messages in which the subject contains both the word "dinner" and the word "movie" </td></tr>
<tr><td>  in:anywhere  </td><td>    Search for messages anywhere in Gmail<br> <b>Messages in</b><strong>Spam</strong> and <strong>Trash</strong> are excluded from searches by default  </td><td>   Example - <strong>in:anywhere movie </strong><br> Meaning - Messages in <strong>All Mail</strong>, <strong>Spam</strong>, and <strong>Trash</strong> that contain the word "movie" </td></tr>
<tr><td>  in:inbox<br> in:trash<br> in:spam  </td><td>    Search for messages in <strong>Inbox</strong>, <strong>Trash</strong>, or <strong>Spam</strong>  </td><td>   Example - <strong>in:trash from:amy</strong><br> Meaning -  Messages from Amy that are in <strong>Trash</strong> </td></tr>
<tr><td>is:starred<br> is:unread<br> is:read<br>  </td><td>    Search for messages that are starred, unread or read </td><td>   Example - <strong> is:read is:starred from:David</strong><br> Meaning - Messages from David that have been read and are marked with a star </td></tr>
<tr><td>  cc:<br> bcc: </td><td>    Used to specify recipients in the <strong>cc:</strong> or <strong>bcc:</strong> fields<br> <b>Search on bcc: cannot retrieve messages on which you were blind carbon copied</</b></td><td>   Example - <strong> cc:david </strong><br> Meaning - Messages that were cc-ed to David </td></tr>
<tr><td>  after:<br> before:<br>  </td><td>    Search for messages sent during a certain period of time<br> <b>Dates must be in yyyy-mm-dd format.</b></td><td>   Example - <strong> after:2004-04-16 before:2004-04-18 </strong><br> Meaning -  Messages sent between April 16, 2004 and April 18, 2004.<br><b>More precisely: Messages sent after 12:00 AM (or 00:00) April 16, 2004 and before April 18, 2004.</b></td></tr>