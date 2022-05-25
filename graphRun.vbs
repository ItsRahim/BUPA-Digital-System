Set oShell = CreateObject ("Wscript.Shell") 
Dim strArgs
strArgs = "cmd /c graph.bat"
oShell.Run strArgs, 0, false