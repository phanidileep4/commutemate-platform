const {app,BrowserWindow}=require('electron');
app.whenReady().then(()=>{const win=new BrowserWindow({width:1200,height:850});win.loadURL(process.env.COMMUTEMATE_MEMBER_URL||'http://localhost:4201');});
