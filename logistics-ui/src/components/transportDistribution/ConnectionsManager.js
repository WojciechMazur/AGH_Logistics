import * as React from "react";
import Paper from "@material-ui/core/Paper/Paper";
import Tabs from "@material-ui/core/Tabs/Tabs";
import Tab from "@material-ui/core/Tab/Tab";
import {withStyles} from "@material-ui/core";
import Table from "@material-ui/core/Table/Table";
import TableHead from "@material-ui/core/TableHead/TableHead";
import TableRow from "@material-ui/core/TableRow/TableRow";
import TableCell from "@material-ui/core/TableCell/TableCell";
import TableBody from "@material-ui/core/TableBody/TableBody";
// import TabContainer from "../../pages/transportDistribution/NodesTable";
import Typography from "@material-ui/core/Typography/Typography";
import Props from '../../pages/transportDistribution/NodesTable'
import Input from "@material-ui/core/Input/Input";
import update from "immutability-helper";

type State = {
    managerCurrentTab: number
}

const TabNames = {
    DISPOSAL: 0,
    LIMITS: 1,
    TRANSPORT_COST: 2,
    PRIORITY: 3,
    SUMMARY: 4
};

function TabContainer(props) {
    return (
        <Typography component="div" style={{ padding: 8 * 3 }}>
            {props.children}
        </Typography>
    );
}

class ConnectionsManager extends React.Component<Props, State> {
    constructor(props) {
        super(props);
        this.state = {
            managerCurrentTab: TabNames.SUMMARY
        };
    }

    handleTabChange = (event, value) => {
        this.setState({managerCurrentTab: value});
    };

    drawConnectionsSummary = () => {
        return <Paper className="ConnectionsManagerPaper">
            <Table className="ConnectionsManagerTable">
                <TableHead>
                    <TableRow>
                        <TableCell>Supplier name</TableCell>
                        <TableCell>Recipient name</TableCell>
                        <TableCell numeric>Units</TableCell>
                        <TableCell numeric>Transport cost</TableCell>
                        <TableCell numeric>Priority</TableCell>
                        <TableCell numeric>Limit</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {this.props.connections.map(elem => {
                        return (
                            <TableRow key={elem.id}>
                                <TableCell component="th" scope="row">{elem.supplier.name}</TableCell>
                                <TableCell>{elem.recipient.name}</TableCell>
                                <TableCell numeric>{elem.attributes['units']}</TableCell>
                                <TableCell numeric>{elem.attributes.transportCost || "None"}</TableCell>
                                <TableCell numeric>{elem.attributes.priority || "None"}</TableCell>
                                <TableCell numeric>{elem.attributes.limit || "None"}</TableCell>
                            </TableRow>
                        );
                    })}
                </TableBody>
            </Table>
        </Paper>
    };


    drawDisposal = () => {
        return <Paper className="ConnectionsDisposal">
            <Table className='ConnectionsDisposalTable'>
                <TableHead>
                    <TableRow>
                        <TableCell/>
                        {this.props.recipients.map(recipient =>
                            <TableCell className={this.props.classes.tableCell} key={"Recipient-" + recipient.id} numeric>{recipient.name}</TableCell>
                        )}
                    </TableRow>
                </TableHead>
                <TableBody>
                    {this.props.suppliers.map(supplier =>
                        <TableRow key={supplier.id}>
                            <TableCell key={"Supplier-" + supplier.id}>{supplier.name}</TableCell>
                            {this.props.recipients.map(recipient => {
                                const connection = this.props.connections.find((connection) => connection.supplier === supplier && connection.recipient === recipient);
                                return <TableCell className={this.props.classes.tableCell} key={supplier.name + '-' + recipient.name}
                                                  numeric>
                                    <Input className={this.props.classes.input} type={"number"}
                                           defaultValue={connection.attributes.units}
                                           onChange={(event) => this.props.dashboardRef.handleUpdateConnection(
                                               update(connection, {
                                                   attributes: {$merge: {
                                                           units: Math.max(event.target.value, 0)
                                                       }
                                                   }}
                                               )
                                           )}
                                    />
                                </TableCell>
                            })
                            }
                        </TableRow>)}
                </TableBody>
            </Table>
        </Paper>
    };

    drawLimits = () => {
        return <Paper className="ConnectionsLimits">
            <Table className='ConnectionsLimitsTable'>
                <TableHead>
                    <TableRow>
                        <TableCell/>
                        {this.props.recipients.map(recipient =>
                            <TableCell className={this.props.classes.tableCell} key={'Recipient-' + recipient.id} numeric>{recipient.name}</TableCell>
                        )}
                    </TableRow>
                </TableHead>
                <TableBody>
                    {this.props.suppliers.map(supplier =>
                        <TableRow key={supplier.id}>
                            <TableCell   key={'Supplier-' + supplier.id}>{supplier.name}</TableCell>
                            {this.props.recipients.map(recipient => {
                                const connection = this.props.connections.find((connection) => connection.supplier === supplier && connection.recipient === recipient);
                                return <TableCell className={this.props.classes.tableCell} key={supplier.name + '-' + recipient.name}
                                                  numeric>
                                    <Input className={this.props.classes.input} type={"number"}
                                           defaultValue={connection.attributes.limit}
                                           onChange={(event) => this.props.dashboardRef.handleUpdateConnection(
                                               update(connection, {
                                                   attributes: {$merge: {
                                                           limit: Math.max(event.target.value, 0)
                                                       }
                                                   }}
                                               )
                                           )}
                                    />
                                </TableCell>
                            })
                            }
                        </TableRow>)}
                </TableBody>
            </Table>
        </Paper>
    };

    drawTransportCosts() {
        return <Paper className="ConnectionsTransportCost">
            <Table className='ConnectionsTransportCost'>
                <TableHead>
                    <TableRow>
                        <TableCell/>
                        {this.props.recipients.map(recipient =>
                            <TableCell className={this.props.classes.tableCell} key={'Recipient-' + recipient.id} numeric>{recipient.name}</TableCell>
                        )}
                    </TableRow>
                </TableHead>
                <TableBody>
                    {this.props.suppliers.map(supplier =>
                        <TableRow key={supplier.id}>
                            <TableCell  key={'Supplier-' + supplier.id}>{supplier.name}</TableCell>
                            {this.props.recipients.map(recipient => {
                                const connection = this.props.connections.find((connection) => connection.supplier === supplier && connection.recipient === recipient);
                                return <TableCell className={this.props.classes.tableCell} key={supplier.name + '-' + recipient.name} numeric >
                                    <Input className={this.props.classes.input} type={"number"}
                                           defaultValue={connection.attributes.transportCost}
                                           onChange={(event) => this.props.dashboardRef.handleUpdateConnection(
                                               update(connection, {
                                                   attributes: {$merge: {
                                                    transportCost: Math.max(event.target.value, 0)
                                                   }
                                                   }}
                                               )
                                           )}
                                    />
                                </TableCell>
                            })
                            }
                        </TableRow>)}
                </TableBody>
            </Table>
        </Paper>
    }

    drawPriority() {
        return <Paper className="ConnectionsPriority">
            <Table className='ConnectionsPriority'>
                <TableHead>
                    <TableRow>
                        <TableCell/>
                        {this.props.recipients.map(recipient =>
                            <TableCell className={this.props.classes.tableCell} key={'Recipient-' + recipient.id} numeric>{recipient.name}</TableCell>
                        )}
                    </TableRow>
                </TableHead>
                <TableBody>
                    {this.props.suppliers.map(supplier =>
                        <TableRow key={supplier.id}>
                            <TableCell  key={'Supplier-' + supplier.id}>{supplier.name}</TableCell>
                            {this.props.recipients.map(recipient => {
                                const connection = this.props.connections.find((connection) => connection.supplier === supplier && connection.recipient === recipient);
                                return <TableCell className={this.props.classes.tableCell} key={supplier.name + '-' + recipient.name} numeric >
                                    <Input className={this.props.classes.input} type={"number"}
                                           defaultValue={connection.attributes.priority}
                                           onChange={(event) => this.props.dashboardRef.handleUpdateConnection(
                                               update(connection, {
                                                   attributes: {$merge: {
                                                           priority: event.target.value
                                                       }
                                                   }}
                                               )
                                           )}
                                    />
                                </TableCell>
                            })
                            }
                        </TableRow>)}
                </TableBody>
            </Table>
        </Paper>
    }

    render() {
        const currentTab = this.state.managerCurrentTab;
        return (
            <Paper className={this.props.classes.root}>
                <Tabs
                    value={this.state.managerCurrentTab}
                    onChange={this.handleTabChange}
                    indicatorColor={"primary"}
                    textColor={"primary"}
                    centered
                >
                    <Tab label="Disposal"/>
                    <Tab label="Limit"/>
                    <Tab label="Transport cost"/>
                    <Tab label="Priority"/>
                    <Tab label="Summary"/>
                </Tabs>
                {currentTab === TabNames.DISPOSAL && <TabContainer>{this.drawDisposal()}</TabContainer>}
                {currentTab === TabNames.LIMITS && <TabContainer>{this.drawLimits()}</TabContainer>}
                {currentTab === TabNames.TRANSPORT_COST && <TabContainer>{this.drawTransportCosts()}</TabContainer>}
                {currentTab === TabNames.PRIORITY && <TabContainer>{this.drawPriority()}</TabContainer>}
                {currentTab === TabNames.SUMMARY && <TabContainer>{this.drawConnectionsSummary()}</TabContainer>}
            </Paper>
        );
    }
}

const styles = {
    root: {
        width: '100%',
        overflowX: 'auto',
    },
    table: {},
    tableCell: {
        minWidth: '3em',
        padding: '0 1em',
        textAlign: 'center'
    },
    input: {
        width: '100%',
        minWidth: '2em',
    }
};

export default withStyles(styles)(ConnectionsManager);