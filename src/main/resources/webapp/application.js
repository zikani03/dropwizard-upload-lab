// ES2015 + ReactJS transpiled via Babel's browser.min.js script
class User {
    constructor(username, password=null) {
        this.username = username;
        this.password = password;
    }
}

const UUID_LENGTH=36;

class FileInfo {
    constructor(fileName, size=0, createdAt=new Date()) {
        this.fileName = fileName;
        this.size = size;
        this.createdAt = createdAt;
    }

    niceName() {
        return this.fileName.substring(UUID_LENGTH);
    }

   sizeHuman() {
        let kbs = Math.ceil(this.size / 1024);

        let mbs = Math.ceil(this.size / (1024 * 1024));

        if (this.size < 1024) return `${this.size} Bytes`;

        if (this.size < (1024 * 1024)) return `${kbs} KB`;

        return `${mbs} MB`;
   }
}

class UploadService {

    constructor() {
    }

    onUploadSuccess(callback=null) {
        if (callback === null) {
            if (this._onUploadSuccess) this._onUploadSuccess();
        } else {
            this._onUploadSuccess = callback;
        }
    }

    onUploadFailed(callback=null) {
        if (callback === null) {
            if (this._onUploadFailed) this._onUploadFailed();
        } else {
            this._onUploadFailed = callback;
        }
    }

    upload(fileData, withAuth=false, token=null) {
        const formData = new FormData();
        const xhr = new XMLHttpRequest();
        const path = window.location + 'api/upload' + (withAuth ? '/auth' : '/noauth');

        formData.append('fileData', fileData);

        xhr.open('POST', path, true);

        if (withAuth) {
            xhr.setRequestHeader('Authorization', 'Bearer ' + token);
        }

        xhr.onreadystatechange = (evt) => {
            const target = evt.target;

            if (target.readyState===4 && (target.status === 200 || target.status === 201)) {
                const data = JSON.parse(target.responseText) || {};

                return this.onUploadSuccess(data);

            } else if (target.readyState===4 && target.status != 200) {
                return this.onUploadFailed(target.responseText);
            }
        };

        xhr.send(formData);
    }
}

const LoginScreen = React.createClass({

    onSubmit: function(evt) {
        evt.preventDefault();

        const xhr = new XMLHttpRequest();
        const path = window.location + 'api/auth';

        const loginData = {
            username: this.refs.username.value,
            password: this.refs.password.value
        };

        xhr.open('POST', path, true);

        xhr.setRequestHeader('Content-Type', 'application/json;charset=utf-8');

        xhr.onreadystatechange = (evt) => {
            const target = evt.target;

            console.log(target);

            if (target.readyState === 4 && target.status === 200) {
                const data = JSON.parse(target.responseText);
                localStorage.setItem('dropwizardupload.token', data.token);
                return data;
            }

            if (target.readyState == 4 && target.status !== 200) {
                console.log('Failed to login');
            }

            return false;
        };

        xhr.send(JSON.stringify(loginData));

        return false;
    },

    render: function() {
        return (
            <form onSubmit={this.onSubmit}>
              <div className="form-group">
                <label>Username</label>
                <input type="text" name="username" ref="username"/>
              </div>
              <div className="form-group">
                <label>Password</label>
                <input type="password" name="password" ref="password" />
              </div>
              <button>Login</button>
            </form>
        );
    }
})
const App = React.createClass({

    getInitialState: function() {
        return {
            withAuth: false,
        }
    },

    componentDidMount: function() {

        this.setState({withAuth: !this.props.noauth});

        this.uploadService = new UploadService();

        this.uploadService.onUploadSuccess(this._onFileUploaded);

        this.uploadService.onUploadFailed((data) => console.log(data));
    },

    _onFileUploaded: function() {
        alert('Uploaded successfully');
    },

    onSubmit: function (evt) {
        evt.preventDefault();
        const fileData = this.refs.fileEl.files[0];
        if (this.state.withAuth) {
            var token = localStorage.getItem('dropwizardupload.token');
            return this.uploadService.upload(fileData, true, token);
        }

        this.uploadService.upload(fileData);

        return false;
    },

    render: function() {
        const login = this.state.withAuth ? (<LoginScreen></LoginScreen>) : 'No Auth needed'; 
        return (
          <div className="wrapper">
            {login}
            <form method="post" onSubmit={this.onSubmit}>
              <label>Upload File</label>
              <input type="file" name="fileData" className="form-control" ref="fileEl" />
              <button>Upload!</button>
            </form>
          </div>
        );
    }
});



const UploadedFilesInfoApp = React.createClass({

    getInitialState: function() {
        return {
            files: [],
        }
    },

    componentDidMount: function() {
        this.refresh();
        setInterval(() => this.refresh.call(this, null), 5000);
    },

    refresh: function() {
        const xhr = new XMLHttpRequest();
        const path = window.location + 'api/upload';

        xhr.open('GET', path, true);

        xhr.onreadystatechange = (evt) => {
            const target = evt.target;
            if (target.readyState === 4 && target.status === 200) {
                const data = JSON.parse(target.responseText);

                const datafiles = data.files.map((f) => new FileInfo(f.fileName, f.size));

                this.setState({ files: datafiles });

                this.render();

                return data;
            }
            if (target.readyState == 4 && target.status !== 200) {
                console.log('Failed to load files');
            }
            return false;
        };

        xhr.send();

        return false;
    },

    render: function() {

        const children = this.state
            .files
            .map((f) => <li><a href={'#/' + f.fileName} title={f.fileName}>{f.niceName()} (<small>{f.sizeHuman()}</small>)</a></li>);

        return (
          <div className="wrapper">
            <h2>Uploaded Files</h2>
            <ul>
              {children}
            </ul>
          </div>
        );
    }
});

ReactDOM.render(
    <App noauth={false} />,
    document.querySelector('#uploadApplication')
);

ReactDOM.render(
    <App noauth={true} />,
    document.querySelector('#uploadApplicationNoAuth')
);

ReactDOM.render(
    <UploadedFilesInfoApp />,
    document.querySelector('#uploadedFilesInfo')
);