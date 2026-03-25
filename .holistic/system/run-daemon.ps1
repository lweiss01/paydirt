$ErrorActionPreference = 'Stop'
$node = 'C:\Program Files\nodejs\node.exe'
$daemon = 'C:\Users\lweis\AppData\Roaming\npm\node_modules\holistic\dist\daemon.js'
$working = 'C:\Users\lweis\Documents\paydirt'
& 'C:\Users\lweis\Documents\paydirt\.holistic\system\restore-state.ps1'
& $node $daemon --interval 30 --agent unknown
