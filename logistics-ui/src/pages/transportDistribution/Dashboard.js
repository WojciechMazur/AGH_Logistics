// @flow
import * as React from 'react';
import classNames from 'classnames';
import {withStyles} from '@material-ui/core/styles';
import CssBaseline from '@material-ui/core/CssBaseline';
import Drawer from '@material-ui/core/Drawer';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import List from '@material-ui/core/List';
import Typography from '@material-ui/core/Typography';
import Divider from '@material-ui/core/Divider';
import IconButton from '@material-ui/core/IconButton';
import MenuIcon from '@material-ui/icons/Menu';
import ChevronLeftIcon from '@material-ui/icons/ChevronLeft';
import {mainListItems} from './listItems';
import NodesTable from './NodesTable';
import {SimpleConnection, ConnectionAttributes, Recipient, Supplier} from "../../types";
import update from 'immutability-helper'
import type {Connection} from "../../types";

const drawerWidth = 240;

const styles = theme => ({
  root: {
    display: 'flex',
  },
  toolbar: {
    paddingRight: 24, // keep right padding when drawer closed
  },
  toolbarIcon: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'flex-end',
    padding: '0 8px',
    ...theme.mixins.toolbar,
  },
  appBar: {
    zIndex: theme.zIndex.drawer + 1,
    transition: theme.transitions.create(['width', 'margin'], {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.leavingScreen,
    }),
  },
  appBarShift: {
    marginLeft: drawerWidth,
    width: `calc(100% - ${drawerWidth}px)`,
    transition: theme.transitions.create(['width', 'margin'], {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.enteringScreen,
    }),
  },
  menuButton: {
    marginLeft: 12,
    marginRight: 36,
  },
  menuButtonHidden: {
    display: 'none',
  },
  title: {
    flexGrow: 1,
  },
  drawerPaper: {
    position: 'relative',
    whiteSpace: 'nowrap',
    width: drawerWidth,
    transition: theme.transitions.create('width', {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.enteringScreen,
    }),
  },
  drawerPaperClose: {
    overflowX: 'hidden',
    transition: theme.transitions.create('width', {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.leavingScreen,
    }),
    width: theme.spacing.unit * 7,
    [theme.breakpoints.up('sm')]: {
      width: theme.spacing.unit * 9,
    },
  },
  appBarSpacer: theme.mixins.toolbar,
  content: {
    flexGrow: 1,
    padding: theme.spacing.unit * 3,
    height: '100vh',
    overflow: 'auto',
  },
  chartContainer: {
    marginLeft: -22,
  },
  tableContainer: {
    height: 320,
  },
  h5: {
    marginBottom: theme.spacing.unit * 2,
  },
});

type Props = {
    classes: Object
}
type State = {
    suppliers: Array<Supplier>,
    recipients: Array<Recipient>,
    connections: Array<SimpleConnection>,
    drawerIsOpen: boolean
}

const mockNodes: {
    suppliers: Array<Supplier>,
    recipients: Array<Recipient>
} = {
    suppliers: [
        new Supplier("A",null, null,null, 50, 5),
        new Supplier("B",null, null,null, 70, 15),
        new Supplier("C",null, null,null, 30, 20)
    ],
    recipients: [
        new Recipient("R1", null, null, null, 20, 27),
        new Recipient("R2", null, null, null, 40, 21),
        new Recipient("R3", null, null, null, 90, 17),
    ]
};

const mockConnections: Array<SimpleConnection> = [
    new SimpleConnection(mockNodes.suppliers[0], mockNodes.recipients[0], new ConnectionAttributes(0,null,null, 3, mockNodes.suppliers[0].purchaseCost, mockNodes.recipients[0].saleProfit)),
    new SimpleConnection(mockNodes.suppliers[0], mockNodes.recipients[1], new ConnectionAttributes(0,null,null, 5, mockNodes.suppliers[0].purchaseCost, mockNodes.recipients[1].saleProfit )),
    new SimpleConnection(mockNodes.suppliers[0], mockNodes.recipients[2], new ConnectionAttributes(0,null,null, 7, mockNodes.suppliers[0].purchaseCost, mockNodes.recipients[2].saleProfit )),

    new SimpleConnection(mockNodes.suppliers[1], mockNodes.recipients[0], new ConnectionAttributes(0,null,null, 12, mockNodes.suppliers[1].purchaseCost, mockNodes.recipients[0].saleProfit)),
    new SimpleConnection(mockNodes.suppliers[1], mockNodes.recipients[1], new ConnectionAttributes(0,null,null, 10, mockNodes.suppliers[1].purchaseCost, mockNodes.recipients[1].saleProfit)),
    new SimpleConnection(mockNodes.suppliers[1], mockNodes.recipients[2], new ConnectionAttributes(0,null,null, 9 , mockNodes.suppliers[1].purchaseCost, mockNodes.recipients[2].saleProfit)),

    new SimpleConnection(mockNodes.suppliers[2], mockNodes.recipients[0], new ConnectionAttributes(0,null,null, 13, mockNodes.suppliers[2].purchaseCost, mockNodes.recipients[0].saleProfit)),
    new SimpleConnection(mockNodes.suppliers[2], mockNodes.recipients[1], new ConnectionAttributes(0,null,null, 3 , mockNodes.suppliers[2].purchaseCost, mockNodes.recipients[1].saleProfit)),
    new SimpleConnection(mockNodes.suppliers[2], mockNodes.recipients[2], new ConnectionAttributes(0,null,null, 9 , mockNodes.suppliers[2].purchaseCost, mockNodes.recipients[2].saleProfit))
];

class Dashboard extends React.Component<Props, State> {
    state: State = {
        drawerIsOpen: false,
        suppliers: mockNodes.suppliers,
        recipients: mockNodes.recipients,
        connections: mockConnections
    };


    addSupplier(supplier: Supplier) {
        if (this.state.suppliers.map(s => s.name.toLowerCase()).includes(supplier.name.toLowerCase())) {
            console.error("Supplier with name " + supplier.name + " already exists");
        } else {
            const updatedConnections = this.state.recipients
                .map(recipient =>
                    new SimpleConnection(supplier, recipient, new ConnectionAttributes(
                        0,
                        Math.max(supplier.limit || 0, recipient.limit || 0),
                        Math.max(supplier.priority || 0, recipient.priority || 0),
                        0,
                        supplier.purchaseCost,
                        recipient.saleProfit))
                );
            this.setState((prevState) => ({
                suppliers: update(prevState.suppliers, {$push: [supplier]}),
                connections: update(prevState.connections, {$push: [...updatedConnections]}).sort(SimpleConnection.compare)
            }));
        }
    }

    addRecipient(recipient: Recipient) {
        if (this.state.recipients.map(r => r.name.toLowerCase()).includes(recipient.name.toLowerCase())) {
            console.error("Recipient with name " + recipient.name + " already exists");
        } else {
            const updatedConnections = this.state.suppliers
                .map(supplier =>
                    new SimpleConnection(supplier, recipient,
                        new ConnectionAttributes(
                            0,
                            Math.max(supplier.limit || 0, recipient.limit || 0),
                            Math.max(supplier.priority || 0, recipient.priority || 0),
                            0,
                            supplier.purchaseCost,
                            recipient.saleProfit))
                );
            this.setState((prevState) => ({
                recipients: update(prevState.recipients, {$push: [recipient]}),
                connections: update(prevState.connections, {$push: [...updatedConnections]}).sort(SimpleConnection.compare)
            }));
        }
    }
    handleRemoveRecipient = (recipient: Recipient) => {
        this.setState({
            recipients: this.state.recipients.filter(r => r !== recipient),
            connections: this.state.connections.filter(c => c.recipient !== recipient)
        })
    };

    handleUpdateConnections = (conn: Array<Connection>) => {
        const connections = conn
            .map(c => c.SimpleConnection || c.MediatorConnection)
            .map(c => {
                // $FlowFixMe
                const purchaseCost = this.state.suppliers.find(s => s.id === c.supplier.id).purchaseCost;
                // $FlowFixMe
                const saleProfit = this.state.recipients.find(r => r.id === c.recipient.id).saleProfit;
                // $FlowFixMe
                const transportCost = this.state.connections.find(cc => cc.id === c.id).attributes.unitTransportCost;
                return update(c, {
                    supplier: {
                        $merge: {
                            purchaseCost: purchaseCost
                        }
                    },
                    recipient: {
                        $merge: {
                            saleProfit: saleProfit
                        }
                    },
                    attributes: {
                        $merge: {
                            unitTransportCost: transportCost,
                            unitPurchaseCost: purchaseCost,
                            unitSaleProfit: saleProfit
                        }
                    }
                })
            });

        const suppliers = connections
            .map(c => c.supplier)
            .filter((value, index, self) => self.map(c => c.id).indexOf(value.id) === index)
            .sort((x, y) => x.name.localeCompare(y.name));

        const recipients = connections
            .map(c => c.recipient)
            .filter((value, index, self) => self.map(c => c.id).indexOf(value.id) === index)
            .sort((x, y) => x.name.localeCompare(y.name));
        this.setState(
            update(this.state, {
                    $merge: {
                        connections: connections,
                        suppliers: suppliers,
                        recipients: recipients
                    }
                },
            ))
    };

    handleRemoveSupplier = (supplier: Supplier) => {
      this.setState({
          suppliers: this.state.suppliers.filter(s => s !== supplier),
          connections: this.state.connections.filter(c => c.supplier !== supplier)
      })
    };

    handleUpdateSupplier = (supplier: Supplier) => {
            const idx = this.state.suppliers.findIndex(s => s.id === supplier.id);
            if(idx === -1){
                console.error("Recipient with id " + supplier.id + " not found")
            } else {
                const updatedConnections = this.state.connections.map(connection => {
                    if(connection.supplier.id === supplier.id){
                        return update(connection, {
                            $merge: {supplier: supplier},
                            attributes: {$merge: {
                                purchaseCost: supplier.purchaseCost,
                                priority: Math.max(supplier.priority || 0, connection.attributes.priority || 0)
                                }}
                        })}else{
                           return connection
                        }
                });
                this.setState({
                    suppliers: update(this.state.suppliers, {
                        $splice: [[idx, 1, supplier]]
                    }),
                    connections: updatedConnections
                });
            }
    };

    handleUpdateRecipient = (recipient: Recipient) => {
        const idx = this.state.recipients.findIndex(r => r.id === recipient.id);
        if(idx === -1){
            console.error("Recipient with id " + recipient.id + " not found")
        } else {
            const updatedConnections = this.state.connections.map(connection => {
                if(connection.recipient.id === recipient.id){
                    return update(connection, {
                        $merge: {recipient: recipient},
                        attributes: {$merge: {
                                saleProfit: recipient.saleProfit,
                                priority: Math.max(recipient.priority || 0, connection.attributes.priority || 0)
                            }}
                    })}else{
                    return connection
                }
            });
            this.setState({
                recipients: update(this.state.recipients, {
                    $splice: [[idx, 1, recipient]]
                }),
                connections: updatedConnections
            });
        }
    };

    handleUpdateConnection = (connection: SimpleConnection) => {
        const idx = this.state.connections.findIndex(c => c.id === connection.id);
        if (idx === -1) {
            console.error("SimpleConnection with id " + connection.id + " not found")
        } else {
            this.setState({
                connections: update(this.state.connections, {
                    $splice: [[idx, 1, connection]]
                })
            })
        }
    };

    handleDrawerOpen = () => {
        this.setState({drawerIsOpen: true});
    };

    handleDrawerClose = () => {
        this.setState({drawerIsOpen: false});
    };

    render() {
        const {classes} = this.props;

        return (
            <React.Fragment>
                <CssBaseline/>
                <div className={classes.root}>
                    <AppBar
                        position="absolute"
                        className={classNames(classes.appBar, this.state.drawerIsOpen && classes.appBarShift)}
                    >
                        <Toolbar disableGutters={!this.state.drawerIsOpen} className={classes.toolbar}>
                            <IconButton
                                color="inherit"
                                aria-label="Open drawer"
                                onClick={this.handleDrawerOpen}
                                className={classNames(
                                    classes.menuButton,
                                    this.state.drawerIsOpen && classes.menuButtonHidden,
                                )}
                            >
                                <MenuIcon/>
                            </IconButton>
                            <Typography
                                component="h1"
                                variant="h6"
                                color="inherit"
                                noWrap
                                className={classes.title}
                            >
                                Dashboard
                            </Typography>
                        </Toolbar>
                    </AppBar>
                    <Drawer
                        variant="permanent"
                        classes={{
                            paper: classNames(classes.drawerPaper, !this.state.drawerIsOpen && classes.drawerPaperClose),
                        }}
                        open={this.state.drawerIsOpen}
                    >
                        <div className={classes.toolbarIcon}>
                            <IconButton onClick={this.handleDrawerClose}>
                                <ChevronLeftIcon/>
                            </IconButton>
                        </div>
                        <Divider/>
                        <List>{mainListItems}</List>
                    </Drawer>
                    <main className={classes.content}>
                        <div className={classes.appBarSpacer}/>
                        <div className={classes.tableContainer}>
                            <NodesTable suppliers={this.state.suppliers}
                                        recipients={this.state.recipients}
                                        connections={this.state.connections}
                                        dashboardRef={this}
                            />
                        </div>
                    </main>
                </div>
            </React.Fragment>
        );
    }
}

export default withStyles(styles)(Dashboard);
