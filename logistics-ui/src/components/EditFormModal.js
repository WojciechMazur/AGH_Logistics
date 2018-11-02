import React from 'react';
import {withStyles} from '@material-ui/core/styles';
import Modal from '@material-ui/core/Modal';
import Button from '@material-ui/core/Button';
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";

const styles = theme => ({
    paper: {
        position: 'absolute',
        margin: 0,
        width: "75%",
        height: "15%",
        backgroundColor: theme.palette.background.paper,
        boxShadow: theme.shadows[5],
        padding: theme.spacing.unit * 4,
        top: "50%",
        left: "50%",
        transform: "translate(-50%, -50%)",
    },
});


class EditFormModal extends React.Component {
    state = {
        open: false,
    };

    handleOpen = () => {
        this.setState({ open: true });
    };

    handleClose = () => {
        this.setState({ open: false });
    };

    render() {
        const { classes } = this.props;

        return (
            <div>
                <Button onClick={this.handleOpen}><FontAwesomeIcon icon="edit"/></Button>
                <Modal
                    open={this.state.open}
                    onClose={this.handleClose}
                >
                    <div className={classes.paper}>
                        {this.props.children}
                    </div>
                </Modal>
            </div>
        );
    }
}

export default withStyles(styles)(EditFormModal);

